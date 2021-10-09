package com.crumbs.orderservice.mapper;

import com.crumbs.lib.entity.CartItem;
import com.crumbs.lib.entity.Customer;
import com.crumbs.lib.repository.MenuItemRepository;
import com.crumbs.orderservice.dto.CartItemDTO;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    private final MenuItemRepository menuItemRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    CartItemMapper(MenuItemRepository menuItemRepository){
        this.menuItemRepository = menuItemRepository;
    }

    public CartItem getCartItem(CartItemDTO cartItemDTO, Customer customer){
       return CartItem.builder()
               .menuItem(menuItemRepository.findById(cartItemDTO.getId()).orElseThrow())
               .preferences(cartItemDTO.getPreferences())
               .customer(customer)
               .build();
    }
}
