package com.crumbs.orderservice.service;

import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.entity.FoodOrder;
import com.crumbs.orderservice.entity.Order;
import com.crumbs.orderservice.entity.Restaurant;
import com.crumbs.orderservice.repository.FoodOrderRepository;
import com.crumbs.orderservice.repository.MenuItemRepository;
import com.crumbs.orderservice.repository.OrderRepository;
import com.crumbs.orderservice.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = { Exception.class })
public class OrderService {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    FoodOrderRepository foodOrderRepository;
    @Autowired
    MenuItemRepository menuItemRepository;
    @Autowired
    RestaurantRepository restaurantRepository;

    public void createOrder(List<CartItemDTO> cartItems){
        List<FoodOrder> foodOrders = getFoodOrders(cartItems);
        Map<Long, List<FoodOrder>> hashMap = createHashMap(foodOrders);

        hashMap.forEach((restaurantId, foodOrdersList) -> {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow();

            Order order = Order.builder()
                    .restaurant(restaurant)
                    .fulfilled(false)
                    .foodOrders(foodOrdersList)
                    .build();

            orderRepository.save(order);
            System.out.println("Order has been created!");
        });
    }

    // Convert cartItems to FoodOrders
    private List<FoodOrder> getFoodOrders(List<CartItemDTO> cartItems){
        return cartItems.stream().map(cartItem -> {
            FoodOrder foodOrder = FoodOrder.builder()
                    .preferences(cartItem.getPreferences())
                    .menuItem(menuItemRepository.findById(cartItem.getId()).orElseThrow())
                    .build();

            return foodOrderRepository.save(foodOrder);
        }).toList();
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