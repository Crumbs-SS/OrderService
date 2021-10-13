package com.crumbs.orderservice.mapper;

import com.crumbs.lib.entity.FoodOrder;
import com.crumbs.lib.repository.MenuItemRepository;
import com.crumbs.orderservice.dto.CartItemDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FoodOrderMapper {


    private final MenuItemRepository menuItemRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    FoodOrderMapper(MenuItemRepository menuItemRepository){
        this.menuItemRepository = menuItemRepository;
    }

    public FoodOrder getMenuItem(CartItemDTO cartItem){
        return FoodOrder.builder()
                .preferences(cartItem.getPreferences())
                .menuItem(menuItemRepository.findById(cartItem.getMenuItem().getId()).orElseThrow())
                .build();
    }

    public List<FoodOrder> getFoodOrders(List<CartItemDTO> cartItems){
        return cartItems.stream()
                .map(this::getMenuItem)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
