package com.crumbs.orderservice.service;

import com.crumbs.lib.entity.*;
import com.crumbs.lib.repository.*;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.CartOrderDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import com.crumbs.orderservice.criteria.OrderSpecification;
import com.crumbs.orderservice.mapper.FoodOrderMapper;
import com.crumbs.orderservice.mapper.OrderDTOMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional(rollbackFor = { Exception.class })
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodOrderMapper foodOrderMapper;
    private final UserDetailsRepository userDetailsRepository;
    private final LocationRepository locationRepository;
    private final OrderDTOMapper orderDTOMapper;
    private final OrderStatusRepository orderStatusRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    OrderService(OrderRepository orderRepository,
            FoodOrderRepository foodOrderRepository,
            RestaurantRepository restaurantRepository,
            FoodOrderMapper foodOrderMapper,
            LocationRepository locationRepository,
            UserDetailsRepository userDetailsRepository,
            OrderStatusRepository orderStatusRepository,
            OrderDTOMapper orderDTOMapper){

        this.orderRepository = orderRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodOrderMapper = foodOrderMapper;
        this.userDetailsRepository = userDetailsRepository;
        this.locationRepository = locationRepository;
        this.orderDTOMapper = orderDTOMapper;
        this.orderStatusRepository = orderStatusRepository;

    }

    public List<Order> createOrder(Long userId, CartOrderDTO cartOrderDTO){

        List<Order> ordersCreated = new ArrayList<>();
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        List<CartItemDTO> cartItems = cartOrderDTO.getCartItems();
        List<FoodOrder> foodOrders = foodOrderMapper.getFoodOrders(cartItems);
        Map<Long, List<FoodOrder>> hashMap = createHashMap(foodOrders);

        hashMap.forEach((restaurantId, foodOrdersList) -> {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow();

            Location deliverLocation = Location.builder()
                    .zipCode("11111")
                    .state("Texas")
                    .city("Houston")
                    .street(cartOrderDTO.getAddress())
                    .build();

            locationRepository.save(deliverLocation);
            Order order = Order.builder()
                    .restaurant(restaurant)
                    .customer(user.getCustomer())
                    .orderStatus(OrderStatus.builder().status("AWAITING_DRIVER").build())
                    .foodOrders(foodOrdersList)
                    .preferences(cartOrderDTO.getPreferences())
                    .phone(cartOrderDTO.getPhone())
                    .createdAt(new Timestamp(new Date().getTime()))
                    .deliveryTime(new Timestamp(new Date().getTime()))
                    .deliveryLocation(deliverLocation)
                    .build();
            foodOrdersList.forEach(foodOrder -> foodOrder.setOrder(order));

            orderRepository.save(order);
            ordersCreated.add(order);

        });

        return ordersCreated;
    }

    public OrdersDTO getOrdersDTO(Long userId, PageRequest pageRequest){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        return OrdersDTO.builder()
                .activeOrders(getOrders(user, "AWAITING_DRIVER", pageRequest))
                .inactiveOrders(getOrders(user, "FULFILLED", pageRequest))
                .build();
    }

    public Page<Order> getOrders(String query, String filterBy, PageRequest pageRequest){
        return orderRepository.findAll(OrderSpecification.getOrdersBySearch(query, filterBy), pageRequest);
    }

    public OrderDTO updateOrder(CartOrderDTO cartOrderDTO, Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow();
        OrderStatus orderStatus = OrderStatus.builder().status(cartOrderDTO.getOrderStatus()).build();

        orderStatus = orderStatusRepository.save(orderStatus);

        order.setPhone(cartOrderDTO.getPhone());
        order.setPreferences(cartOrderDTO.getPreferences());
        order.getDeliveryLocation().setStreet(cartOrderDTO.getAddress());
        order.setOrderStatus(orderStatus);
        order.setDeliveryTime(cartOrderDTO.getDeliveryTime());


        orderRepository.save(order);
        return orderDTOMapper.getOrderDTO(order);
    }

    public OrderDTO updateOrder(CartOrderDTO cartOrderDTO, Long userId, Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.setPhone(cartOrderDTO.getPhone());
        order.setPreferences(cartOrderDTO.getPreferences());
        order.getDeliveryLocation().setStreet(cartOrderDTO.getAddress());

        if(cartOrderDTO.getCartItems() != null){
            order.getFoodOrders().forEach(foodOrderRepository::delete);
            List<FoodOrder> foodOrders = foodOrderMapper.getFoodOrders(cartOrderDTO.getCartItems());
            foodOrders.forEach(foodOrder -> foodOrder.setOrder(order));

            order.setFoodOrders(foodOrders);
        }
        orderRepository.save(order);

        return orderDTOMapper.getOrderDTO(order);
    }

    public OrderDTO deleteOrder(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow();
        OrderStatus orderStatus = OrderStatus.builder().status("DELETED").build();
        order.setOrderStatus(orderStatusRepository.save(orderStatus));

        orderRepository.save(order);
        return orderDTOMapper.getOrderDTO(order);
    }

    private Page<Order> getOrders(UserDetails user, String status, PageRequest pageRequest){
        OrderStatus orderStatus = OrderStatus.builder().status(status).build();
        return orderRepository.findOrderByOrderStatusAndCustomer(orderStatus, user.getCustomer(), pageRequest);
    }


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
