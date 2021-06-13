package com.crumbs.orderservice.service;


import com.crumbs.orderservice.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {Exception.class})
public class CartService {

    @Autowired
    CartItemRepository cartItemRepository;


}
