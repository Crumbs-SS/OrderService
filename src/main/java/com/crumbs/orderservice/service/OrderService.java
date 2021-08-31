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
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import lombok.SneakyThrows;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional(rollbackFor = {Exception.class})
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
    private final PaymentRepository paymentRepository;

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
                 DriverStateRepository driverStateRepository,
                 PaymentRepository paymentRepository) {

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
        this.paymentRepository = paymentRepository;
    }

    public String locationToString(Location location) {
        return location.getStreet() + ", " + location.getCity() + ", " + location.getState() + " " + location.getZipCode() + ", United States";
    }

    public List<Order> createOrder(Long userId, CartOrderDTO cartOrderDTO) {

        List<Order> ordersCreated = new ArrayList<>();
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        List<CartItemDTO> cartItems = cartOrderDTO.getCartItems();
        List<FoodOrder> foodOrders = foodOrderMapper.getFoodOrders(cartItems);
        Map<Long, List<FoodOrder>> hashMap = createHashMap(foodOrders);

        hashMap.forEach((restaurantId, foodOrdersList) -> {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow();
            String[] address = cartOrderDTO.getAddress().split(", ");
            Location deliverLocation = Location.builder()
                    .zipCode("11111")
                    .state(address[2])
                    .city(address[1])
                    .street(address[0])
                    .build();

            locationRepository.save(deliverLocation);

            Payment payment = paymentRepository.findPaymentByStripeID(cartOrderDTO.getStripeID());
            payment.setStatus("succeeded");
            payment = paymentRepository.save(payment);

            DistanceMatrixElement result = null;
            try {
                result = getDistanceAndTime(locationToString(restaurant.getLocation()), locationToString(deliverLocation));
                String deliveryTime = result.duration.toString();
                //in future, check if customer location and restaurant location is more than 50 miles apart for example, or do some sort of distance check?
                //also for driver, only view orders in his area? add story to backlog to handle this
                String deliveryDistance = result.distance.toString();
                Float deliveryPay = Float.parseFloat(deliveryDistance.split("mi")[0].trim()) * 0.7F;

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
                        .deliveryTime(deliveryTime)
                        .deliveryDistance(deliveryDistance)
                        .deliveryPay(deliveryPay)
                        .payment(payment)
                        .build();
                foodOrdersList.forEach(foodOrder -> foodOrder.setOrder(order));
                orderRepository.save(order);
                ordersCreated.add(order);
            } catch (InterruptedException | IOException | ApiException e) {
                e.printStackTrace();
            }
        });

        return ordersCreated;
    }

    public OrdersDTO getOrdersDTO(Long userId, PageRequest pageRequest) {
        UserDetails user = userDetailsRepository.findById(userId).orElseThrow();
        return OrdersDTO.builder()
                .activeOrders(getOrders(user, "AWAITING_DRIVER", pageRequest))
                .inactiveOrders(getOrders(user, "FULFILLED", pageRequest))
                .build();
    }

    public Page<Order> getOrders(String query, String filterBy, PageRequest pageRequest) {
        return orderRepository.findAll(OrderSpecification.getOrdersBySearch(query, filterBy), pageRequest);
    }

    @SneakyThrows
    public OrderDTO updateOrder(CartOrderDTO cartOrderDTO, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();


        //once Elijah does location dropdown, I will add appropriate equals check
//        DistanceMatrixElement result = getDistanceAndTime(locationToString(order.getRestaurant().getLocation()), locationToString(order.getDeliveryLocation()));
//        String deliveryTime = result.duration.toString();
//        String deliveryDistance = result.distance.toString();
//        Float deliveryPay = Float.parseFloat(deliveryDistance.split("mi")[0].trim()) * 0.7F;
//
//        order.setDeliveryDistance(deliveryDistance);
//        order.setDeliveryTime(deliveryTime);
//        order.setDeliveryPay(deliveryPay);

        order.setPhone(cartOrderDTO.getPhone());
        order.setPreferences(cartOrderDTO.getPreferences());
        order.getDeliveryLocation().setStreet(cartOrderDTO.getAddress());

        if (cartOrderDTO.getDeliverySlot() != null)
            order.setDeliverySlot(cartOrderDTO.getDeliverySlot());

        if (cartOrderDTO.getOrderStatus() != null) {
            OrderStatus orderStatus = OrderStatus.builder().status(cartOrderDTO.getOrderStatus()).build();
            orderStatus = orderStatusRepository.save(orderStatus);
            order.setOrderStatus(orderStatus);
        }

        orderRepository.save(order);
        return orderDTOMapper.getOrderDTO(order);
    }

    public OrderDTO deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        OrderStatus orderStatus = OrderStatus.builder().status("DELETED").build();
        order.setOrderStatus(orderStatusRepository.save(orderStatus));

        orderRepository.save(order);
        return orderDTOMapper.getOrderDTO(order);
    }

    private Page<Order> getOrders(UserDetails user, String status, PageRequest pageRequest) {
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

    public void cancelOrder(Long order_id) {
        orderRepository.findById(order_id).orElseThrow(NoSuchElementException::new);
        orderRepository.deleteById(order_id);
    }

    public List<Order> getAvailableOrders() {
        OrderStatus orderStatus = OrderStatus.builder().status("AWAITING_DRIVER").build();
        return orderRepository.findOrderByOrderStatus(orderStatus);
    }

    synchronized public Order acceptOrder(Long driver_id, Long order_id) {

        Order order = orderRepository.findById(order_id).orElseThrow(NoSuchElementException::new);
        Driver driver = driverRepository.findById(driver_id).orElseThrow(NoSuchElementException::new);

        if (!order.getOrderStatus().getStatus().equals("AWAITING_DRIVER"))
            throw new RuntimeException("Order no longer available");
        if (orderRepository.findDriverAcceptedOrder(driver.getId()).size() > 1)
            throw new RuntimeException("Driver is already delivering an order");

        OrderStatus orderStatus = orderStatusRepository.findById("DELIVERING").orElseThrow();
        DriverState driverState = driverStateRepository.findById("BUSY").orElseThrow();

        driver.setState(driverState);
        driverRepository.save(driver);

        order.setDriver(driver);
        order.setOrderStatus(orderStatus);

        return orderRepository.save(order);
    }

    public DistanceMatrixElement getDistanceAndTime(String origin, String destination) throws InterruptedException, ApiException, IOException {

        //put as environment variable
        final String API_KEY = "AIzaSyBlmGGAkSVOeBCNMab09DnxefDmH4hfdt4";
        final GeoApiContext context = new GeoApiContext.Builder().apiKey(API_KEY).build();

        String[] origins = {origin};
        String[] destinations = {destination};

        DistanceMatrix distanceMatrix = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations).units(Unit.IMPERIAL).await();
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
        Float totalPay;
        if(driver.getTotalPay() != null)
            totalPay = driver.getTotalPay() + order.getDeliveryPay();
        else
            totalPay = order.getDeliveryPay();
        driver.setTotalPay(totalPay);
        driverRepository.save(driver);

        order.setDeliveredAt(new Timestamp(System.currentTimeMillis()));
        order.setOrderStatus(orderStatus);

        return orderRepository.save(order);
    }
    public Order getAcceptedOrder(Long driver_id){
        return orderRepository.findDriverAcceptedOrder(driver_id).get(0);
    }
}
