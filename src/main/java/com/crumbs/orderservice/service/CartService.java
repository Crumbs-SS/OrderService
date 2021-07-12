package com.crumbs.orderservice.service;


import com.crumbs.lib.entity.CartItem;
import com.crumbs.lib.entity.UserDetails;
import com.crumbs.lib.repository.CartItemRepository;
import com.crumbs.lib.repository.UserDetailsRepository;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.mapper.CartItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final CartItemMapper cartItemMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    CartService(
        CartItemRepository cartItemRepository,
        UserDetailsRepository userDetailsRepository,
        CartItemMapper cartItemMapper){

        this.cartItemRepository = cartItemRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.cartItemMapper = cartItemMapper;
    }


    public List<CartItem> createCartItem(Long userId, CartItemDTO cartItemDTO){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        CartItem cartItem = cartItemMapper.getCartItem(cartItemDTO, user.getCustomer());

        cartItemRepository.save(cartItem);
        return getCartItems(userId);
    }

    public List<CartItem> getCartItems(Long userId){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        return user.getCustomer().getCartItems();
    }


    public void deleteCart(Long userId){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();

        user.getCustomer()
                .getCartItems()
                .forEach(cartItemRepository::delete);

        user.getCustomer().getCartItems().clear();
        userDetailsRepository.save(user);
    }

    public List<CartItem> removeItem(Long userId, Long menuItemId){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        List<CartItem> cartItems = new ArrayList<>();

        user.getCustomer()
                .getCartItems()
                .forEach(cartItem -> {
                    if(cartItem.getMenuItem().id.equals(menuItemId))
                        cartItemRepository.delete(cartItem);
                    else
                        cartItems.add(cartItem);

                });

        user.getCustomer().setCartItems(cartItems);
        userDetailsRepository.save(user);

        return user.getCustomer().getCartItems();
    }

}
