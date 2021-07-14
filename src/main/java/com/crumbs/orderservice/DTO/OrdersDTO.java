package com.crumbs.orderservice.DTO;

import com.crumbs.lib.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersDTO {
    private Page<Order> activeOrders;
    private Page<Order> inactiveOrders;
}
