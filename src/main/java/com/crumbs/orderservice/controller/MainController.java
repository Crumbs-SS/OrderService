package com.crumbs.orderservice.controller;

import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import com.crumbs.orderservice.entity.CartItem;
import com.crumbs.orderservice.entity.Order;
import com.crumbs.orderservice.service.CartService;
import com.crumbs.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@Validated
public class MainController {

    @Autowired
    OrderService orderService;
    @Autowired
    CartService cartService;

    @GetMapping("customers/{id}/orders")
    public ResponseEntity<Object> getOrders(@PathVariable Integer id) {
        OrdersDTO orders = orderService.getOrders(id);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("customers/{id}/orders")
    public ResponseEntity<Object> createOrder(
            @Validated @RequestBody OrderDTO orderDTO,
            @PathVariable Integer id){

        List<Order> orders = orderService.createOrder(id, orderDTO);
        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @PostMapping("customers/{id}/cart")
    public ResponseEntity<Object> addToCart(
            @Validated @RequestBody CartItemDTO cartItemDTO,
            @PathVariable Integer id){

        List<CartItem> cartItems = cartService.createCartItem(id, cartItemDTO);
        return new ResponseEntity<>(cartItems, HttpStatus.CREATED);
    }

    @GetMapping("customers/{id}/cart")
    public ResponseEntity<Object> getCartItems(@PathVariable Integer id) {
        List<CartItem> cartItems = cartService.getCartItems(id);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    @DeleteMapping("customers/{id}/cart")
    public ResponseEntity<Object> deleteCart(@PathVariable Integer id){
        cartService.deleteCart(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("customers/{userId}/cart/{cartId}")
    public ResponseEntity<Object> removeItem(
            @PathVariable Integer userId,
            @PathVariable Long cartId){
        List<CartItem> cartItems = cartService.removeItem(userId, cartId);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }
}

