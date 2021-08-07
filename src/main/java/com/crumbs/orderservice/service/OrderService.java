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
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    public String locationToString(Location location){
        return location.getStreet() + ", " + location.getCity() + ", " + location.getState() + " " + location.getZipCode() + ", United States";
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

            try {
               DistanceMatrixElement result = getDistanceAndTime(locationToString(restaurant.getLocation()), locationToString(deliverLocation));



            Order order = Order.builder()
                    .restaurant(restaurant)
                    .customer(user.getCustomer())
                    .orderStatus(OrderStatus.builder().status("AWAITING_DRIVER").build())
                    .foodOrders(foodOrdersList)
                    .preferences(cartOrderDTO.getPreferences())
                    .phone(cartOrderDTO.getPhone())
                    .createdAt(new Timestamp(new Date().getTime()))
                    .deliverySlot(new Timestamp(new Date().getTime()))
                    .deliveryLocation(deliverLocation)
                    .deliveryTime(result.duration.toString())
                    .deliveryDistance(result.distance.toString())
                    .deliveryPay(BigDecimal.valueOf(result.duration.inSeconds))
                    .build();
            foodOrdersList.forEach(foodOrder -> foodOrder.setOrder(order));
                orderRepository.save(order);
                ordersCreated.add(order);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


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
        order.setDeliverySlot(cartOrderDTO.getDeliverySlot());


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


    private Map<Long, List<FoodOrder>> createHashMap(List<FoodOrder> foodOrders) {
        Map<Long, List<FoodOrder>> hashMap = new HashMap<>();

        foodOrders.forEach(foodOrder -> {
            Long restaurantId = foodOrder.getMenuItem().getRestaurant().getId();
            List<FoodOrder> foodOrdersInHash = hashMap.get(restaurantId) != null ?
                    hashMap.get(restaurantId) : new ArrayList<>();

            if (!foodOrdersInHash.isEmpty()) {
                foodOrdersInHash.add(foodOrder);
            } else {
                foodOrdersInHash.add(foodOrder);
                hashMap.put(restaurantId, foodOrdersInHash);
            }
        });

        return hashMap;
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

        if(!order.getOrderStatus().getStatus().equals("AWAITING_DRIVER"))
            throw new RuntimeException("Order no longer available");

        OrderStatus orderStatus = orderStatusRepository.findById("DELIVERING").get();
        DriverState driverState = driverStateRepository.findById("BUSY").get();

        driver.setState(driverState);
        driverRepository.save(driver);

        order.setDriver(driver);
        order.setOrderStatus(orderStatus);

        return orderRepository.save(order);
    }

    public DistanceMatrixElement getDistanceAndTime(String origin, String destination) throws InterruptedException, ApiException, IOException {

        final String API_KEY = "AIzaSyBlmGGAkSVOeBCNMab09DnxefDmH4hfdt4";
        final GeoApiContext context = new GeoApiContext.Builder().apiKey(API_KEY).build();

        String[] origins = new String[1];
        String[] destinations = new String[1];

        origins[0] = origin;
        destinations[0] = destination;

        DistanceMatrix distanceMatrix = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations).await();
        DistanceMatrixRow[] distanceMatrixRows = distanceMatrix.rows;

       return distanceMatrixRows[0].elements[0];

    }

    public void setPickedUpAt(Long order_id) {
        Order order = orderRepository.findById(order_id).orElseThrow(NoSuchElementException::new);
        order.setPickedUpAt(new Timestamp(System.currentTimeMillis()));
        orderRepository.save(order);
    }

    public Order fulfilOrder(Long order_id) {

        Order order = orderRepository.findById(order_id).orElseThrow(NoSuchElementException::new);
        Driver driver = order.getDriver();

        OrderStatus orderStatus = orderStatusRepository.findById("FULFILLED").get();
        DriverState driverState = driverStateRepository.findById("AVAILABLE").get();

        driver.setState(driverState);
        driverRepository.save(driver);

        order.setDeliveredAt(new Timestamp(System.currentTimeMillis()));
        order.setOrderStatus(orderStatus);

        return orderRepository.save(order);

    }
}
