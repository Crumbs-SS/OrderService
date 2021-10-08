package com.crumbs.orderservice.service;

import com.crumbs.lib.entity.CartItem;
import com.crumbs.lib.entity.MenuItem;
import com.crumbs.lib.entity.UserDetails;
import com.crumbs.lib.repository.CartItemRepository;
import com.crumbs.lib.repository.UserDetailsRepository;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.MockUtil;
import com.crumbs.orderservice.mapper.CartItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class CartServiceTest {

    @Autowired CartService cartService;

    @MockBean CartItemRepository cartItemRepository;
    @MockBean UserDetailsRepository userDetailsRepository;
    @MockBean CartItemMapper cartItemMapper;

    @BeforeEach
    void beforeEach(){
        UserDetails userDetails = MockUtil.getUserDetails();
        CartItem cartItem = MockUtil.getCartItem();
        CartItemDTO cartItemDTO = MockUtil.getCartItemDTO();

        Mockito.when(userDetailsRepository.findByUsername(userDetails.getUsername()))
                .thenReturn(Optional.of(userDetails));
        Mockito.when(cartItemMapper.getCartItem(cartItemDTO, userDetails.getCustomer()))
                .thenReturn(cartItem);
        Mockito.when(cartItemRepository.save(cartItem))
                .thenReturn(cartItem);
        Mockito.when(userDetailsRepository.save(userDetails)).thenReturn(userDetails);

        Mockito.doNothing().when(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    void createCartItem() {
        UserDetails userDetails = MockUtil.getUserDetails();
        CartItemDTO cartItemDTO = MockUtil.getCartItemDTO();

        assertEquals(cartService.createCartItem(userDetails.getUsername(), cartItemDTO).size(),
                userDetails.getCustomer().getCartItems().size());
    }

    @Test
    void getCartItems() {
        UserDetails userDetails = MockUtil.getUserDetails();

        assertEquals(cartService.getCartItems(userDetails.getUsername()).size(),
                userDetails.getCustomer().getCartItems().size());
    }

    @Test
    void deleteCart() {
        UserDetails userDetails = MockUtil.getUserDetails();

        cartService.deleteCart(userDetails.getUsername());
        Mockito.verify(cartItemRepository).deleteAll(any(List.class));
    }

    @Test
    void removeItem() {
        UserDetails userDetails = MockUtil.getUserDetails();
        MenuItem menuItem = MockUtil.getMenuItem();

        assertEquals(cartService.removeItem(userDetails.getUsername(), menuItem.getId()).size(),
                userDetails.getCustomer().getCartItems().size() - 1);


    }
}