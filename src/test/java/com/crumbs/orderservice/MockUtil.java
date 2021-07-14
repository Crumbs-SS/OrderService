package com.crumbs.orderservice;

import com.crumbs.lib.entity.*;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.CartOrderDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;

public class MockUtil {

    public static Order getOrder(){
        return Order.builder()
                .id(-1L)
                .deliveryLocation(getLocation())
                .foodOrders(new ArrayList<>())
                .build();
    }

    public static Customer getCustomer(){
        return Customer.builder()
                .id(-1L)
                .cartItems(new ArrayList<>(List.of(getCartItem())))
                .orders(List.of(getOrder()))
                .build();
    }

    public static CartItem getCartItem(){
        return CartItem.builder()
                .id(-1L)
                .menuItem(getMenuItem())
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

    public static UserDetails getUserDetails(){
        return UserDetails.builder()
                .id(-1L)
                .customer(getCustomer())
                .phone("1234567890")
                .email("mock@gmail")
                .firstName("Mock")
                .lastName("Bean")
                .password("123456789012345678901234567890123456789012345678901234567890")
                .username("mockbean12")
                .build();
    }

    public static OrdersDTO getOrdersDTO(){
        return OrdersDTO.builder()
                .activeOrders(new PageImpl<>(List.of(getOrder())))
                .build();
    }

    public static OrderDTO getOrderDTO(){
        return OrderDTO.builder()
                .deliveryLocation(getLocation())
                .foodOrders(new ArrayList<>())
                .id(-1L)
                .build();
    }

    public static Location getLocation(){
        return Location.builder()
                .street("test lane")
                .id(-1L)
                .build();
    }
}
