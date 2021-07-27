package com.crumbs.orderservice.service;

import com.crumbs.lib.entity.*;
import com.crumbs.lib.repository.*;
import com.crumbs.orderservice.DTO.CartItemDTO;
import com.crumbs.orderservice.DTO.CartOrderDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import com.crumbs.orderservice.mapper.FoodOrderMapper;
import com.crumbs.orderservice.mapper.OrderDTOMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final DriverRepository driverRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DriverStateRepository driverStateRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    OrderService(OrderRepository orderRepository,
            FoodOrderRepository foodOrderRepository,
            RestaurantRepository restaurantRepository,
            FoodOrderMapper foodOrderMapper,
            LocationRepository locationRepository,
            UserDetailsRepository userDetailsRepository,
            OrderDTOMapper orderDTOMapper,
            DriverRepository driverRepository,
            OrderStatusRepository orderStatusRepository,
            DriverStateRepository driverStateRepository){

        this.orderRepository = orderRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodOrderMapper = foodOrderMapper;
        this.userDetailsRepository = userDetailsRepository;
        this.locationRepository = locationRepository;
        this.orderDTOMapper = orderDTOMapper;
        this.driverRepository = driverRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.driverStateRepository = driverStateRepository;
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

    public OrdersDTO getOrders(Long userId, PageRequest pageRequest){
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        return OrdersDTO.builder()
                .activeOrders(getOrders(user, "AWAITING_DRIVER", pageRequest))
                .inactiveOrders(getOrders(user, "FULFILLED", pageRequest))
                .build();
    }

    private Page<Order> getOrders(UserDetails user, String status, PageRequest pageRequest){
        OrderStatus orderStatus = OrderStatus.builder().status(status).build();
        return orderRepository.findOrderByOrderStatusAndCustomer(orderStatus, user.getCustomer(), pageRequest);
    }

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

    public OrderDTO updateOrder(CartOrderDTO cartOrderDTO, Long userId, Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.setPhone(cartOrderDTO.getPhone());
        order.setPreferences(cartOrderDTO.getPreferences());
        order.getDeliveryLocation().setStreet(cartOrderDTO.getAddress());

        if(cartOrderDTO.getCartItems() != null){
            //Remove old foodOrders
            order.getFoodOrders().forEach(foodOrderRepository::delete);

            // Create FoodOrder for each cartItem from the orderDto
            List<FoodOrder> foodOrders = foodOrderMapper.getFoodOrders(cartOrderDTO.getCartItems());
            foodOrders.forEach(foodOrder -> foodOrder.setOrder(order));

            order.setFoodOrders(foodOrders);
        }
        orderRepository.save(order);

        return orderDTOMapper.getOrderDTO(order);
    }

    public void cancelOrder(Long order_id){
        Order order = orderRepository.findById(order_id).orElseThrow(NoSuchElementException::new);
        orderRepository.deleteById(order_id);
    }

    public List<Order> getAvailableOrders(){
        OrderStatus orderStatus = OrderStatus.builder().status("AWAITING_DRIVER").build();
        return orderRepository.findOrderByOrderStatus(orderStatus);
    }
    public Order acceptOrder(Long driver_id, Long order_id ){

        Order order = orderRepository.findById(order_id).orElseThrow(NoSuchElementException::new);
        Driver driver = driverRepository.findById(driver_id).orElseThrow(NoSuchElementException::new);

        OrderStatus orderStatus = orderStatusRepository.findById("DELIVERING").get();
        DriverState driverState = driverStateRepository.findById("BUSY").get();

        driver.setState(driverState);
        driverRepository.save(driver);

        order.setDriver(driver);
        order.setOrderStatus(orderStatus);

        return orderRepository.save(order);
    }

}
