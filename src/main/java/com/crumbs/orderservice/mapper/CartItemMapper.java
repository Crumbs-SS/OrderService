package com.crumbs.orderservice.mapper;

import com.crumbs.lib.entity.CartItem;
import com.crumbs.lib.entity.Customer;
import com.crumbs.lib.repository.MenuItemRepository;
import com.crumbs.orderservice.DTO.CartItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {
    @Autowired
    MenuItemRepository menuItemRepository;


    public CartItem getCartItem(CartItemDTO cartItemDTO, Customer customer){
       return CartItem.builder()
               .menuItem(menuItemRepository.findById(cartItemDTO.getId()).orElseThrow())
               .preferences(cartItemDTO.getPreferences())
               .customer(customer)
               .build();
    }
}
