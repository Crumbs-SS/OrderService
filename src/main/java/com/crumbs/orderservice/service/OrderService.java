package com.crumbs.orderservice.service;

import com.crumbs.lib.entity.*;
import com.crumbs.lib.repository.*;
import com.crumbs.orderservice.dto.*;
import com.crumbs.orderservice.criteria.OrderSpecification;
import com.crumbs.orderservice.mapper.FoodOrderMapper;
import com.crumbs.orderservice.mapper.OrderDTOMapper;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Unit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodOrderMapper foodOrderMapper;
    private final UserDetailsRepository userDetailsRepository;
    private final LocationRepository locationRepository;
    private final OrderDTOMapper orderDTOMapper;
    private final DriverRepository driverRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DriverStateRepository driverStateRepository;
    private final PaymentRepository paymentRepository;
    private final DriverRatingRepository driverRatingRepository;


    public String locationToString(Location location) {
        return location.getStreet() + ", " + location.getCity() + ", " + location.getState() + ", United States";
    }

    public List<Order> createOrder(String username, CartOrderDTO cartOrderDTO) {
        List<Order> ordersCreated = new ArrayList<>();
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow();
        List<CartItemDTO> cartItems = cartOrderDTO.getCartItems();
        List<FoodOrder> foodOrders = foodOrderMapper.getFoodOrders(cartItems);

        Map<Long, List<FoodOrder>> hashMap = createHashMap(foodOrders);

        hashMap.forEach((restaurantId, foodOrdersList) -> {
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();

            Payment payment = paymentRepository.findPaymentByStripeID(cartOrderDTO.getStripeID());
            payment.setStatus("succeeded");
            payment = paymentRepository.save(payment);

            Location deliveryLocation = getDeliveryLocation(cartOrderDTO);

            DistanceMatrixElement result = null;
            try {
                result = getDistanceAndTime(locationToString(restaurant.getLocation()),
                        locationToString(deliveryLocation));
            } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
            } catch (ApiException | IOException ignored) {}

            assert result != null;

            String deliveryTime = result.duration.toString();
            String deliveryDistance = result.distance.toString();
            float deliveryPay;
            try{
                 deliveryPay = Float.parseFloat(deliveryDistance.split("mi")[0].trim()) * 0.7F;
            }catch(Exception ignored) {
                 deliveryPay = 0F;
            }

            Order order = Order.builder()
                    .restaurant(restaurant)
                    .customer(user.getCustomer())
                    .orderStatus(OrderStatus.builder().status("AWAITING_DRIVER").build())
                    .foodOrders(foodOrdersList)
                    .preferences(cartOrderDTO.getPreferences())
                    .phone(cartOrderDTO.getPhone())
                    .createdAt(new Timestamp(new Date().getTime()))
                    .deliverySlot(new Timestamp(new Date().getTime()))
                    .deliveryLocation(deliveryLocation)
                    .deliveryTime(deliveryTime)
                    .deliveryDistance(deliveryDistance)
                    .deliveryPay(deliveryPay)
                    .payment(payment)
                    .build();

            foodOrdersList.forEach(foodOrder -> foodOrder.setOrder(order));
            orderRepository.save(order);
            ordersCreated.add(order);
        });

        return ordersCreated;
    }

    public OrdersDTO getOrdersDTO(String username, PageRequest pageRequest) {
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow();
        return OrdersDTO.builder()
                .activeOrders(getOrders(user, "AWAITING_DRIVER", pageRequest))
                .inactiveOrders(getOrders(user, "FULFILLED", pageRequest))
                .build();
    }

    public Page<Order> getOrders(String query, String filterBy, PageRequest pageRequest) {
        return orderRepository.findAll(OrderSpecification.getOrdersBySearch(query, filterBy), pageRequest);
    }

    public OrderDTO updateOrder(CartOrderDTO cartOrderDTO, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.setPhone(cartOrderDTO.getPhone());
        order.setPreferences(cartOrderDTO.getPreferences());
        order.setDeliveryLocation(getDeliveryLocation(cartOrderDTO));

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
        if ("FULFILLED".equals(order.getOrderStatus().getStatus()))
            revokeLoyaltyPoints(order);

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

    public List<Order> getAvailableOrders() {
        OrderStatus orderStatus = OrderStatus.builder().status("AWAITING_DRIVER").build();
        return orderRepository.findOrderByOrderStatus(orderStatus);
    }

    public synchronized Order acceptOrder(String username, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(NoSuchElementException::new);
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow();
        Driver driver = Optional.of(user.getDriver()).orElseThrow();

        if (!order.getOrderStatus().getStatus().equals("AWAITING_DRIVER"))
            throw new RuntimeException("Order no longer available");
        if (orderRepository.findDriverAcceptedOrder(username).size() > 1)
            throw new RuntimeException("Driver is already delivering an order");

        OrderStatus orderStatus = orderStatusRepository.findById("DELIVERING").orElseThrow();
        DriverState driverState = driverStateRepository.findById("BUSY").orElseThrow();

        driver.setState(driverState);
        driverRepository.save(driver);

        order.setDriver(driver);
        order.setOrderStatus(orderStatus);
        return orderRepository.save(order);
    }

    public Order abandonOrder(String username){
        List<Order> orders = orderRepository.findDriverAcceptedOrder(username);
        Order order = null;
        if (!orders.isEmpty()){
            order = orders.get(0);
            order.setOrderStatus(OrderStatus.builder().status("AWAITING_DRIVER").build());
            order.setDriver(null);
            orderRepository.save(order);
        }
        return order;
    }

    public DistanceMatrixElement getDistanceAndTime(String origin, String destination) throws InterruptedException, ApiException, IOException  {
        final String API_KEY = System.getenv("GMAPS_API_KEY");
        final GeoApiContext context = new GeoApiContext.Builder().apiKey(API_KEY).build();

        String[] origins = {origin};
        String[] destinations = {destination};

        DistanceMatrix distanceMatrix = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations).units(Unit.IMPERIAL).await();
        DistanceMatrixRow[] distanceMatrixRows = distanceMatrix.rows;
        return distanceMatrixRows[0].elements[0];

    }

    public Order setPickedUpAt(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(NoSuchElementException::new);
        order.setPickedUpAt(new Timestamp(System.currentTimeMillis()));
        orderRepository.save(order);

        return order;
    }

    public Order fulfilOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(NoSuchElementException::new);
        Driver driver = order.getDriver();

        OrderStatus orderStatus = orderStatusRepository.findById("FULFILLED").orElseThrow();
        DriverState driverState = driverStateRepository.findById("AVAILABLE").orElseThrow();

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
        orderRepository.save(order);
        order.getCustomer().setLoyaltyPoints((getLoyaltyPointsForOrder(order))
                + order.getCustomer().getLoyaltyPoints());

        userDetailsRepository.save(order.getCustomer().getUserDetails());

        return order;
    }

    public Order getAcceptedOrder(String username){
        return orderRepository.findDriverAcceptedOrder(username).get(0);
    }

    public DriverRating getDriverRating(Long orderId){
        return driverRatingRepository.findDriverRatingByOrderId(orderId);
    }

    public DriverRating submitDriverRating(Long orderId, RatingDTO ratingDTO) {

        Order order = orderRepository.findById(orderId).orElseThrow(NoSuchElementException::new);
        DriverRating rating = new DriverRating();

        rating.setOrder(order);
        rating.setDriver(order.getDriver());
        rating.setCustomer(order.getCustomer());
        rating.setRating(ratingDTO.getRating());
        rating.setDescription(ratingDTO.getDescription());
        driverRatingRepository.save(rating);

        order.setDriverRating(rating);
        orderRepository.save(order);

        return rating;

    }

    private void revokeLoyaltyPoints(Order order){
        Customer customer = order.getCustomer();
        Integer customerPoints = customer.getLoyaltyPoints();
        Integer orderPoints = getLoyaltyPointsForOrder(order);

        customer.setLoyaltyPoints(Math.max(0, customerPoints - orderPoints));
        userDetailsRepository.save(order.getCustomer().getUserDetails());
    }

    private Integer getLoyaltyPointsForOrder(Order order){
        return (int)(order.getFoodOrders().stream()
                .map(foodOrder -> foodOrder.getMenuItem().getPrice())
                .reduce(0F, Float::sum)/5);
    }

    private Location getDeliveryLocation(CartOrderDTO cartOrderDTO){
        String[] address = cartOrderDTO.getAddress().split(", ");
        Location deliveryLocation = Location.builder()
                .state(address[2])
                .city(address[1])
                .street(address[0])
                .build();
        locationRepository.save(deliveryLocation);
        return deliveryLocation;
    }

    public List<Order> getPendingOrders(String username){
        UserDetails user = userDetailsRepository.findByUsername(username).orElseThrow(EntityNotFoundException::new);
        Owner owner = Optional.of(user.getOwner()).orElseThrow(EntityNotFoundException::new);
 
        List<Restaurant> restaurants = owner.getRestaurants();
        List<Order> orders = new ArrayList<>();
        restaurants.forEach(restaurant -> orders.addAll(orderRepository.findRestaurantPendingOrders(restaurant.getId())));
        return orders;
    }
}
