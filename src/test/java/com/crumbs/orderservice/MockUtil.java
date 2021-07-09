package com.crumbs.orderservice;

import com.crumbs.lib.entity.*;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.CartOrderDTO;

import java.util.ArrayList;
import java.util.List;

public class MockUtil {

    public static Order getOrder(){
        return Order.builder()
                .id(-1L)
                .build();
    }

    public static Customer getCustomer(){
        return Customer.builder()
                .id(-1L)
                .build();
    }

    public static CartItem getCartItem(){
        return CartItem.builder()
                .id(-1L)
                .build();
    }

    public static Restaurant getRestaurant(){
        return Restaurant.builder()
                .name("Restaurant Test")
                .id(-1L)
                .build();
    }

    public static MenuItem getMenuItem(){
        return MenuItem.builder()
                .name("Test")
                .id(-1L)
                .description("Menuitem for testing")
                .price(-1.99F)
                .restaurant(getRestaurant())
                .build();

    }

    public static CartItemDTO getCartItemDTO(){
        return CartItemDTO.builder()
                .menuItem(getMenuItem())
                .preferences("")
                .id(-1L)
                .build();
    }

    public static CartOrderDTO getCartOrderDTO(){
        return CartOrderDTO.builder()
                .cartItems(List.of(getCartItemDTO()))
                .phone("1234567890")
                .address("Testing Lane")
                .preferences("")
                .build();
    }
}
