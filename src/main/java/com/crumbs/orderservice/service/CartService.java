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


    public List<CartItem> createCartItem(String username, CartItemDTO cartItemDTO){
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow();
        CartItem cartItem = cartItemMapper.getCartItem(cartItemDTO, user.getCustomer());

        cartItemRepository.save(cartItem);
        return getCartItems(username);
    }

    public List<CartItem> getCartItems(String username){
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow();
        return user.getCustomer().getCartItems();
    }


    public void deleteCart(String username){
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow();
        cartItemRepository.deleteAll(user.getCustomer().getCartItems());
        user.getCustomer().getCartItems().clear();
        userDetailsRepository.save(user);
    }

    public List<CartItem> removeItem(String username, Long menuItemId){
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow();
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
