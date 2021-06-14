package com.crumbs.orderservice.mapper;

import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.entity.FoodOrder;
import com.crumbs.orderservice.repository.FoodOrderRepository;
import com.crumbs.orderservice.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FoodOrderMapper {
    @Autowired FoodOrderRepository foodOrderRepository;
    @Autowired MenuItemRepository menuItemRepository;

    public FoodOrder getMenuItem(CartItemDTO cartItem){
        return FoodOrder.builder()
                .preferences(cartItem.getPreferences())
                .menuItem(menuItemRepository.findById(cartItem.getMenuItem().getId()).orElseThrow())
                .build();
    }

    public List<FoodOrder> getFoodOrders(List<CartItemDTO> cartItems){
        return cartItems.stream()
                .map(cartItem -> foodOrderRepository.save(getMenuItem(cartItem)))
                .toList();
    }
}
