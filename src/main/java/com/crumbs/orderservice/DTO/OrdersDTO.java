package com.crumbs.orderservice.DTO;

import com.crumbs.lib.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersDTO {
    private List<Order> orders;
    private List<Order> activeOrders;
    private List<Order> inactiveOrders;
}
