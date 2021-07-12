package com.crumbs.orderservice.DTO;

import com.crumbs.lib.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    @NotNull
    @NotEmpty
    private Long id;

    @NotNull
    @NotEmpty
    private String phone;

    @NotNull
    @NotEmpty
    private String preferences;

    @NotNull
    @NotEmpty
    private Timestamp deliveryTime;

    @NotNull
    @NotEmpty
    private Timestamp createdAt;

    private DriverRating driverRating;
    private RestaurantRating restaurantRating;
    private OrderStatus orderStatus;
    private Driver driver;
    private Customer customer;
    private Payment payment;
    private Location deliveryLocation;
    private Restaurant restaurant;
    private List<FoodOrder> foodOrders = new ArrayList<>();

}
