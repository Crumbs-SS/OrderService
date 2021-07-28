package com.crumbs.orderservice.service;

import com.crumbs.lib.entity.*;
import com.crumbs.lib.repository.*;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.CartOrderDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import com.crumbs.orderservice.MockUtil;
import com.crumbs.orderservice.mapper.FoodOrderMapper;
import com.crumbs.orderservice.mapper.OrderDTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @MockBean OrderRepository orderRepository;
    @MockBean FoodOrderRepository foodOrderRepository;
    @MockBean RestaurantRepository restaurantRepository;
    @MockBean FoodOrderMapper foodOrderMapper;
    @MockBean UserDetailsRepository userDetailsRepository;
    @MockBean LocationRepository locationRepository;
    @MockBean OrderDTOMapper orderDTOMapper;

    @BeforeEach
    void beforeEach(){
        UserDetails userDetails = MockUtil.getUserDetails();
        CartOrderDTO cartOrderDTO = MockUtil.getCartOrderDTO();
        Restaurant restaurant = MockUtil.getRestaurant();
        Order order = MockUtil.getOrder();
        OrderDTO orderDTO = MockUtil.getOrderDTO();


        Mockito.when(userDetailsRepository.findById(userDetails.getId()))
                .thenReturn(Optional.of(userDetails));
        Mockito.when(foodOrderMapper.getFoodOrders(cartOrderDTO.getCartItems()))
                .thenReturn(new ArrayList<>());
        Mockito.when(locationRepository.save(any(Location.class)))
                .thenReturn(null);
        Mockito.when(restaurantRepository.findById(restaurant.getId()))
                .thenReturn(Optional.of(restaurant));
        Mockito.when(orderRepository.save(any(Order.class)))
                .thenReturn(MockUtil.getOrder());
        Mockito.when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        Mockito.when(orderDTOMapper.getOrderDTO(order))
                .thenReturn(orderDTO);

        Mockito.doNothing().when(foodOrderRepository).delete(any(FoodOrder.class));

    }

    @Test
    void createOrder() {
        UserDetails userDetails = MockUtil.getUserDetails();
        CartOrderDTO cartOrderDTO = MockUtil.getCartOrderDTO();

        assertEquals(orderService.createOrder(userDetails.getId(), cartOrderDTO).size(), 0);
    }

    @Test
    void getOrders() {
        UserDetails userDetails = MockUtil.getUserDetails();
        OrdersDTO ordersDTO = MockUtil.getOrdersDTO();
        PageRequest pageRequest = PageRequest.of(0, 4);

        Mockito.when(orderRepository.findOrderByOrderStatusAndCustomer(any(OrderStatus.class),
                any(Customer.class), any(PageRequest.class))).thenReturn(ordersDTO.getActiveOrders());

        assertEquals(orderService.getOrdersDTO(userDetails.getId(), pageRequest).getActiveOrders().getNumberOfElements(),
                ordersDTO.getActiveOrders().getNumberOfElements());
    }

    @Test
    void updateOrder() {
        Order order = MockUtil.getOrder();
        CartOrderDTO cartOrderDTO = MockUtil.getCartOrderDTO();
        UserDetails userDetails = MockUtil.getUserDetails();
        OrderDTO orderDTO = MockUtil.getOrderDTO();

        assertEquals(orderService.updateOrder(cartOrderDTO, userDetails.getId(), order.getId()).getFoodOrders().size(),
                orderDTO.getFoodOrders().size());
    }
}