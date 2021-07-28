package com.crumbs.orderservice.controller;

import com.crumbs.lib.entity.CartItem;
import com.crumbs.lib.entity.Order;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.CartOrderDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import com.crumbs.orderservice.service.CartService;
import com.crumbs.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@Validated
public class MainController {

    private final OrderService orderService;
    private final CartService cartService;

    MainController(OrderService orderService, CartService cartService){
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("customers/{id}/orders")
    public ResponseEntity<Object> getOrders(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue="3") Integer size
    ){
        OrdersDTO orders = orderService.getOrdersDTO(id, PageRequest.of(page, size));
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("orders")
    public ResponseEntity<Object> getOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue="3") Integer size,
            @RequestParam(defaultValue="") String query,
            @RequestParam(defaultValue="") String filterBy

    ){
        Page<Order> orders = orderService.getOrders(query, filterBy, PageRequest.of(page, size));
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
    @PostMapping("customers/{id}/orders")
    public ResponseEntity<Object> createOrder(
            @Validated @RequestBody CartOrderDTO cartOrderDTO,
            @PathVariable Long id){

        List<Order> orders = orderService.createOrder(id, cartOrderDTO);
        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @PostMapping("customers/{id}/cart")
    public ResponseEntity<Object> addToCart(
            @Validated @RequestBody CartItemDTO cartItemDTO,
            @PathVariable Long id){

        List<CartItem> cartItems = cartService.createCartItem(id, cartItemDTO);
        return new ResponseEntity<>(cartItems, HttpStatus.CREATED);
    }

    @PutMapping("customers/{customerId}/order/{orderId}")
    public ResponseEntity<Object> updateOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @Validated @RequestBody CartOrderDTO cartOrderDTO
    ){

        OrderDTO order = orderService.updateOrder(cartOrderDTO, customerId, orderId);
        return new ResponseEntity<>(order, HttpStatus.NO_CONTENT);
    }

    @GetMapping("customers/{id}/cart")
    public ResponseEntity<Object> getCartItems(@PathVariable Long id) {
        List<CartItem> cartItems = cartService.getCartItems(id);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    @DeleteMapping("customers/{id}/cart")
    public ResponseEntity<Object> deleteCart(@PathVariable Long id){
        cartService.deleteCart(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("customers/{userId}/cart/{cartId}")
    public ResponseEntity<Object> removeItem(
            @PathVariable Long userId,
            @PathVariable Long cartId){
        List<CartItem> cartItems = cartService.removeItem(userId, cartId);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }
}

