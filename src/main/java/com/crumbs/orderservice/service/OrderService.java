package com.crumbs.orderservice.service;

import com.crumbs.lib.repository.UserDetailsRepository;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import com.crumbs.orderservice.entity.*;
import com.crumbs.orderservice.mapper.FoodOrderMapper;
import com.crumbs.orderservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = { Exception.class })
public class OrderService {

    @Autowired OrderRepository orderRepository;
    @Autowired FoodOrderRepository foodOrderRepository;
    @Autowired MenuItemRepository menuItemRepository;
    @Autowired RestaurantRepository restaurantRepository;
    @Autowired FoodOrderMapper foodOrderMapper;
    @Autowired UserDetailsRepository userDetailsRepository;

    public List<Order> createOrder(Integer userId, OrderDTO orderDTO){

        List<Order> ordersCreated = new ArrayList<>();
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        List<CartItemDTO> cartItems = orderDTO.getCartItems();
        List<FoodOrder> foodOrders = foodOrderMapper.getFoodOrders(cartItems);
        Map<Long, List<FoodOrder>> hashMap = createHashMap(foodOrders);

        hashMap.forEach((restaurantId, foodOrdersList) -> {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow();

            Order order = Order.builder()
                    .restaurant(restaurant)
                    .customer(user.getCustomer())
                    .fulfilled(false)
                    .foodOrders(foodOrdersList)
                    .preferences(orderDTO.getPreferences())
                    .phone(orderDTO.getPhone())
                    .address(orderDTO.getAddress())
                    .build();

            orderRepository.save(order);
            ordersCreated.add(order);

            foodOrdersList.forEach(foodOrder -> {
                foodOrder.setOrder(order);
                foodOrderRepository.save(foodOrder);
            });
        });

        return ordersCreated;
    }

    public OrdersDTO getOrders(Integer userId){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();

        return OrdersDTO.builder()
                .orders(user.getCustomer().getOrders())
                .activeOrders(getOrders(userId, false))
                .inactiveOrders(getOrders(userId, true))
                .build();
    }

    private List<Order> getOrders(Integer userId, Boolean fulfilled){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        return orderRepository.findOrderByFulfilledAndCustomer(fulfilled, user.getCustomer());
    }

    // Key, Value pair object
    // Expected: RestaurantId: List<FoodOrder>
    private Map<Long, List<FoodOrder>> createHashMap(List<FoodOrder> foodOrders){
        Map<Long, List<FoodOrder>> hashMap = new HashMap<>();

        foodOrders.forEach(foodOrder -> {
            Long restaurantId = foodOrder.getMenuItem().getRestaurant().getId();
            List<FoodOrder> foodOrdersInHash = hashMap.get(restaurantId) != null ?
                    hashMap.get(restaurantId) : new ArrayList<>();

            if(!foodOrdersInHash.isEmpty()){
                foodOrdersInHash.add(foodOrder);
            }else {
                foodOrdersInHash.add(foodOrder);
                hashMap.put(restaurantId, foodOrdersInHash);
            }
        });

        return hashMap;
    }
}
