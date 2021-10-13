package com.crumbs.orderservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.crumbs.lib.entity.*;
import com.crumbs.orderservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockUtil {

    public static Order getOrder(){
        return Order.builder()
                .id(-1L)
                .orderStatus(OrderStatus.builder().status("FULFILLED").build())
                .deliveryLocation(getLocation())
                .foodOrders(new ArrayList<>())
                .customer(getOrderCustomer())
                .deliveryPay(1F)
                .driver(getOrderDriver())
                .build();
    }

    public static DriverRating getDriverRating(){
        return DriverRating.builder()
                .rating(3)
                .build();
    }

    public static Owner getOwner(){
        return Owner.builder()
                .id(-1L)
                .restaurants(List.of(getRestaurant()))
                .build();
    }

    public static RatingDTO getRatingDTO(){
        return RatingDTO.builder()
                .description("Test Stuff")
                .rating(3)
                .build();
    }

    private static Driver getOrderDriver() {
        return Driver.builder()
                .id(-1L)
                .totalPay(0F)
                .build();
    }
    // Avoids circular dependency between a customer's order and the order
    private static Customer getOrderCustomer() {
        return Customer.builder()
                .id(-1L)
                .loyaltyPoints(0)
                .cartItems(new ArrayList<>(List.of(getCartItem())))
                .build();
    }

    public static Customer getCustomer(){
        return Customer.builder()
                .id(-1L)
                .loyaltyPoints(0)
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

    public static OrderStatus getOrderStatus(){
        return OrderStatus.builder()
                .status("AWAITING_DRIVER")
                .build();
    }

    public static Restaurant getRestaurant(){
        return Restaurant.builder()
                .name("Restaurant Test")
                .location(getLocation())
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
                .cartItems(List.of(getCartItemDTO(), getCartItemDTO(), getCartItemDTO()))
                .phone("1234567890")
                .address("Testing, Lane, Something")
                .orderStatus("AWAITING_DRIVER")
                .stripeID("StripeID")
                .preferences("")
                .deliverySlot(new Timestamp(12348565L))
                .build();
    }

    public static UserDetails getUserDetails(){
        return UserDetails.builder()
                .id(-1L)
                .customer(getCustomer())
                .owner(getOwner())
                .driver(getDriver())
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
                .activeOrders(getOrders())
                .inactiveOrders(getOrders())
                .build();
    }

    public static Page<Order> getOrders(){
        return new PageImpl<>(List.of(getOrder()));
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
                .street("test")
                .city("lane")
                .state("something")
                .id(-1L)
                .build();
    }

    public static Payment getPayment() {
        return Payment.builder()
                .id(-1L)
                .status("")
                .stripeID("StripeID")
                .amount("123")
                .build();
    }

    public static Driver getDriver(){
        return Driver.builder().totalPay(5F).build();
    }

    public static PageRequest getPageRequest(){
        return PageRequest.of(0, 10);
    }

    public  static String createMockJWT(String role){
        final long EXPIRATION_TIME = 900_000;
        String token;

        Algorithm algorithm = Algorithm.HMAC256(System.getenv("JWT_SECRET"));
        token = JWT.create()
                .withAudience("crumbs")
                .withIssuer("Crumbs")
                .withClaim("role", role)
                .withSubject("correctUsername")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);

        return token;
    }


}
