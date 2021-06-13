package com.crumbs.orderservice.controller;

import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.entity.MenuItem;
import com.crumbs.orderservice.entity.Order;
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

    @GetMapping("customers/{id}/orders")
    public ResponseEntity<Object> getOrders(@PathVariable Integer id) {
        List<Order> orders = orderService.getOrders(id);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("customers/{id}/orders")
    public ResponseEntity<Object> createOrder(
            @Validated @RequestBody List<CartItemDTO> cartItems,
            @PathVariable Integer id){

        orderService.createOrder(id, cartItems);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}

