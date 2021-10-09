package com.crumbs.orderservice.mapper;


import com.crumbs.lib.entity.Order;
import com.crumbs.orderservice.dto.OrderDTO;
import org.springframework.stereotype.Component;

@Component
public class OrderDTOMapper {

    public OrderDTO getOrderDTO(Order order){
        return OrderDTO.builder()
                .id(order.getId())
                .phone(order.getPhone())
                .preferences(order.getPreferences())
                .deliverySlot(order.getDeliverySlot())
                .createdAt(order.getCreatedAt())
                .driverRating(order.getDriverRating())
                .restaurantRating(order.getRestaurantRating())
                .orderStatus(order.getOrderStatus())
                .driver(order.getDriver())
                .customer(order.getCustomer())
                .payment(order.getPayment())
                .deliveryLocation(order.getDeliveryLocation())
                .restaurant(order.getRestaurant())
                .foodOrders(order.getFoodOrders())
                .build();
    }

}
