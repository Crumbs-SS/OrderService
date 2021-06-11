package com.crumbs.orderservice.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Validated
public class MainController {
    @GetMapping("/orders")
    public String getOrders(){
        return "Hello World!";
    }
}

