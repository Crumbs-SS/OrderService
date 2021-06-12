package com.crumbs.orderservice.controller;

import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.entity.MenuItem;
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

    @GetMapping("/orders")
    public String getOrders() {
        return "Hello World!";
    }

    @PostMapping("/orders")
    public ResponseEntity<Object> createOrder(@Validated @RequestBody List<CartItemDTO> cartItems){
        orderService.createOrder(cartItems);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}

