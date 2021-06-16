package com.crumbs.orderservice.service;


import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.entity.CartItem;
import com.crumbs.orderservice.entity.UserDetails;
import com.crumbs.orderservice.mapper.CartItemMapper;
import com.crumbs.orderservice.repository.CartItemRepository;
import com.crumbs.orderservice.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class CartService {

    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    UserDetailsRepository userDetailsRepository;
    @Autowired
    CartItemMapper cartItemMapper;


    public List<CartItem> createCartItem(Integer userId, CartItemDTO cartItemDTO){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        CartItem cartItem = cartItemMapper.getCartItem(cartItemDTO, user.getCustomer());

        cartItemRepository.save(cartItem);
        return getCartItems(userId);
    }

    public List<CartItem> getCartItems(Integer userId){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();

        return user.getCustomer().getCartItems();
    }

    public void deleteCart(Integer userId){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();

        user.getCustomer()
                .getCartItems()
                .forEach(cartItem -> cartItemRepository.delete(cartItem));
        user.getCustomer().getCartItems().clear();
        userDetailsRepository.save(user);
    }

    public List<CartItem> removeItem(Integer userId, Long menuItemId){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        List<CartItem> cartItems = new ArrayList<>();

        user.getCustomer()
                .getCartItems()
                .forEach(cartItem -> {
                    if(cartItem.getMenuItem().id.equals(menuItemId))
                        cartItemRepository.delete(cartItem);
                    else{
                        cartItems.add(cartItem);
                    }
                });

        user.getCustomer().setCartItems(cartItems);
        userDetailsRepository.save(user);

        return user.getCustomer().getCartItems();
    }

}
