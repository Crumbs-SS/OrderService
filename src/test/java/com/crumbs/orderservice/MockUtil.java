package com.crumbs.orderservice;

import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.entity.Customer;
import com.crumbs.orderservice.entity.MenuItem;
import com.crumbs.orderservice.entity.Order;
import com.crumbs.orderservice.entity.UserDetails;

import java.util.ArrayList;
import java.util.List;

public class MockUtil {

    public static Customer getCustomer(){
        return Customer.builder()
                .id(1)
                .build();
    }

    public static UserDetails getUserDetails(){
        return UserDetails.builder()
                .customer(getCustomer())
                .id(1)
                .email("1.1@gmail.com")
                .firstName("Mock")
                .lastName("Doe")
                .password("123456")
                .username("mockdoe")
                .build();
    }

    public static List<Order> getOrders(){
        List<Order> orders = new ArrayList<>();

        for (long i = 0L; i < 3; i++) {
            Order order = getOrder();
            order.setId(i);
            orders.add(order);
        }

        return orders;
    }

    public static Order getOrder(){
        return Order.builder()
                .customer(getCustomer())
                .address("123 ABC")
                .fulfilled(false)
                .phone("1234567890")
                .preferences("")
                .build();
    }

    public static OrderDTO getOrderDTO(){
        return OrderDTO.builder()
                .address("123 ABC")
                .phone("1234567890")
                .preferences("Ring doorbell")
                .build();

    }

    public static MenuItem getMenuItem(){
        return MenuItem.builder()
                .id(1L)
                .build();
    }

    public static CartItemDTO getCartItemDTO(){
        return CartItemDTO.builder()
                .menuItem(getMenuItem())
                .id(1L)
                .preferences("")
                .build();
    }
}
