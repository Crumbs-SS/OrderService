package com.crumbs.orderservice.entity;

import com.crumbs.lib.entity.Customer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "restaurant_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties("menuItems")
    private Restaurant restaurant;

    @ManyToOne
    private Location location;

    private Boolean fulfilled;
    private String preferences;
    private String phone;

    private String address;

    @ManyToOne
    @JsonIgnoreProperties("orders")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("order")
    private List<FoodOrder> foodOrders = new ArrayList<>();
}
