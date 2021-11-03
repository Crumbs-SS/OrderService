package com.crumbs.orderservice.controller;

import com.crumbs.lib.entity.CartItem;
import com.crumbs.lib.entity.Order;
import com.crumbs.orderservice.dto.*;
import com.crumbs.orderservice.service.CartService;
import com.crumbs.orderservice.service.OrderService;
import com.google.maps.model.DistanceMatrixElement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@PreAuthorize("isAuthenticated()")
@RequestMapping("/order-service")
public class MainController {
    private final OrderService orderService;
    private final CartService cartService;

    MainController(OrderService orderService, CartService cartService){
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @GetMapping("/customers/{username}/orders")
    public ResponseEntity<Object> getOrders(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue="3") Integer size
    ){
        OrdersDTO orders = orderService.getOrdersDTO(username,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/orders")
    public ResponseEntity<Object> getOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue="3") Integer size,
            @RequestParam(defaultValue="") String query,
            @RequestParam(defaultValue="") String filterBy,
            @RequestParam(defaultValue="id") String sortBy,
            @RequestParam(defaultValue="asc") String orderBy

    ){
        Sort.Direction direction = "asc".equals(orderBy) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Order> orders = orderService.getOrders(query, filterBy, pageRequest);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }


    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @PostMapping("/customers/{username}/orders")
    public ResponseEntity<Object> createOrder(
            @Validated @RequestBody CartOrderDTO cartOrderDTO,
            @PathVariable String username){

        List<Order> orders = orderService.createOrder(username, cartOrderDTO);
        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @PostMapping("/customers/{username}/cart")
    public ResponseEntity<Object> addToCart(
            @Validated @RequestBody CartItemDTO cartItemDTO,
            @PathVariable String username){

        List<CartItem> cartItems = cartService.createCartItem(username, cartItemDTO);
        return new ResponseEntity<>(cartItems, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #username == authentication.principal)")
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<Object> updateOrder(
            @PathVariable Long orderId,
            @Validated @RequestBody CartOrderDTO cartOrderDTO,
            @RequestHeader(value = "Username", required = false) String username
    ){
      OrderDTO orderDTO = orderService.updateOrder(cartOrderDTO, orderId);
      return new ResponseEntity<>(orderDTO, HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @GetMapping("/customers/{username}/cart")
    public ResponseEntity<Object> getCartItems(@PathVariable String username) {
        List<CartItem> cartItems = cartService.getCartItems(username);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @DeleteMapping("/customers/{username}/cart")
    public ResponseEntity<Object> deleteCart(@PathVariable String username){
        cartService.deleteCart(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @DeleteMapping("/customers/{username}/cart/{cartId}")
    public ResponseEntity<Object> removeItemFromCart(
            @PathVariable String username,
            @PathVariable Long cartId){
        List<CartItem> cartItems = cartService.removeItem(username, cartId);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #username == authentication.principal)")
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Object> deleteOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Username", required = false) String username
    ) {
        OrderDTO orderDTO = orderService.deleteOrder(id);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('DRIVER') and #username == authentication.principal")
    @GetMapping("/drivers/{username}/orders/available")
    public ResponseEntity<Object> getAvailableOrders(@PathVariable String username){
        List<Order> orders = orderService.getAvailableOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DRIVER') and #username == authentication.principal)")
    @GetMapping("/drivers/{username}/accepted-order")
    public ResponseEntity<Object> getAcceptedOrder(@PathVariable String username){
        Order order = orderService.getAcceptedOrder(username);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DRIVER') and #username == authentication.principal)")
    @PostMapping("/drivers/{username}/accepted-order")
    public ResponseEntity<Object> acceptOrder(@PathVariable String username, @RequestBody Long orderId){
        return new ResponseEntity<>(orderService.acceptOrder(username, orderId), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DRIVER') and #username == authentication.principal)")
    @PutMapping("/drivers/{username}/order/{orderId}/pickUp")
    public ResponseEntity<Object> setPickedUpAt(
            @PathVariable Long orderId,
            @PathVariable String username){
        orderService.setPickedUpAt(orderId);
        return new ResponseEntity<>("Set Picked Up At successful", HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DRIVER') and #username == authentication.principal)")
    @PutMapping("/drivers/{username}/order/{orderId}")
    public ResponseEntity<Object> fulfilOrder(@PathVariable Long orderId, @PathVariable String username){
        orderService.fulfilOrder(orderId);

        return new ResponseEntity<>("Order fulfilled", HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/orders/{orderId}/driver-rating")
    public ResponseEntity<Object> getDriverRating(@PathVariable Long orderId){
        return new ResponseEntity<>(orderService.getDriverRating(orderId), HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/orders/{orderId}/restaurant-rating")
    public ResponseEntity<Object> getRestaurantRating(@PathVariable Long orderId){
        return new ResponseEntity<>(orderService.getRestaurantRating(orderId), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @PostMapping("/orders/{orderId}/driver-rating")
    public ResponseEntity<Object> submitDriverRating(
            @PathVariable Long orderId,
            @Validated @RequestBody RatingDTO rating,
            @RequestHeader("Username") String username){
        return new ResponseEntity<>(orderService.submitDriverRating(orderId, rating), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('CUSTOMER') and #username == authentication.principal")
    @PostMapping("/orders/{orderId}/restaurant-rating")
    public ResponseEntity<Object> submitRestaurantRating(
            @PathVariable Long orderId,
            @Validated @RequestBody RatingDTO rating,
            @RequestHeader("Username") String username){
        return new ResponseEntity<>(orderService.submitRestaurantRating(orderId, rating), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DRIVER') and #username == authentication.principal)")
    @DeleteMapping("/drivers/{username}/accepted-order")
    public ResponseEntity<Object> abandonOrder(@PathVariable String username){
        Order order = orderService.abandonOrder(username);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('OWNER') and #username == authentication.principal)")
    @GetMapping("/owners/{username}/restaurants/orders")
    public ResponseEntity<Object> getPendingOrders(@PathVariable String username){
        return new ResponseEntity<>(orderService.getPendingOrders(username), HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/")
    public ResponseEntity<Object> getDistanceMatrixElement(@RequestBody DistanceMatrixDTO distanceMatrixDTO){
        DistanceMatrixElement distanceMatrixElement = orderService.getDistanceAndTime(distanceMatrixDTO);
        return new ResponseEntity<>(distanceMatrixElement, HttpStatus.OK);
    }
}
