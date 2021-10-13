package com.crumbs.orderservice.dto;

import com.crumbs.lib.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersDTO {
    private Page<Order> activeOrders;
    private Page<Order> inactiveOrders;
}
