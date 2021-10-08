package com.crumbs.orderservice.service;

import com.crumbs.lib.entity.*;
import com.crumbs.lib.repository.*;
import com.crumbs.orderservice.DTO.CartOrderDTO;
import com.crumbs.orderservice.DTO.OrderDTO;
import com.crumbs.orderservice.DTO.OrdersDTO;
import com.crumbs.orderservice.DTO.RatingDTO;
import com.crumbs.orderservice.MockUtil;
import com.crumbs.orderservice.mapper.OrderDTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    @MockBean UserDetailsRepository userDetailsRepository;
    @MockBean LocationRepository locationRepository;
    @MockBean OrderDTOMapper orderDTOMapper;
    @MockBean OrderStatusRepository orderStatusRepository;
    @MockBean DriverStateRepository driverStateRepository;
    @MockBean DriverRepository driverRepository;
    @MockBean DriverRatingRepository driverRatingRepository;
    @MockBean MenuItemRepository menuItemRepository;
    @MockBean PaymentRepository paymentRepository;

    @BeforeEach
    void beforeEach(){
        UserDetails userDetails = MockUtil.getUserDetails();
        Restaurant restaurant = MockUtil.getRestaurant();
        Order order = MockUtil.getOrder();
        OrderDTO orderDTO = MockUtil.getOrderDTO();
        OrderStatus orderStatus = MockUtil.getOrderStatus();
        Page<Order> orderPage = MockUtil.getOrders();
        MenuItem menuItem = MockUtil.getMenuItem();
        Payment payment = MockUtil.getPayment();

        Mockito.when(userDetailsRepository.findById(userDetails.getId()))
                .thenReturn(Optional.of(userDetails));
        Mockito.when(userDetailsRepository.findByUsername(userDetails.getUsername()))
                .thenReturn(Optional.of(userDetails));
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
        Mockito.when(orderStatusRepository.save(orderStatus))
                .thenReturn(orderStatus);
        Mockito.when(orderStatusRepository.findById(any(String.class)))
                .thenReturn(Optional.of(orderStatus));
        Mockito.when(driverStateRepository.findById(any(String.class)))
                .thenReturn(Optional.of(DriverState.builder().build()));
        Mockito.when(userDetailsRepository.save(any(UserDetails
                .class))).thenReturn(userDetails);
        Mockito.when(orderRepository.findDriverAcceptedOrder(any(String.class)))
                .thenReturn(List.of(order));
        Mockito.when(driverRepository.save(any(Driver.class)))
                .thenReturn(MockUtil.getDriver());
        Mockito.when(orderRepository.findOrderByOrderStatus(any(OrderStatus.class)))
                .thenReturn(List.of(order));
        Mockito.when(driverRatingRepository.findDriverRatingByOrderId(order.getId()))
                .thenReturn(MockUtil.getDriverRating());
        Mockito.when(driverRatingRepository.save(any(DriverRating.class)))
                .thenReturn(MockUtil.getDriverRating());
        Mockito.when(orderRepository
                .findOrderByOrderStatusAndCustomer(
                        any(OrderStatus.class),
                        any(Customer.class),
                        any(PageRequest.class)))
                .thenReturn(orderPage);
        Mockito.when(orderRepository.findRestaurantPendingOrders(restaurant.getId()))
                .thenReturn(List.of(order));
        Mockito.when(menuItemRepository.findById(menuItem.getId()))
                .thenReturn(Optional.of(menuItem));

        Mockito.doNothing().when(foodOrderRepository).delete(any(FoodOrder.class));
        Mockito.when(paymentRepository.findPaymentByStripeID(payment.getStripeID()))
                .thenReturn(payment);
        Mockito.when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

    }

    @Test
    void createOrder() {
        String username = MockUtil.getUserDetails().getUsername();
        CartOrderDTO cartOrderDTO = MockUtil.getCartOrderDTO();

        assertEquals(orderService.createOrder(username, cartOrderDTO).size(), 1);
    }

    @Test
    void getOrders() {
        UserDetails userDetails = MockUtil.getUserDetails();
        OrdersDTO ordersDTO = MockUtil.getOrdersDTO();
        PageRequest pageRequest = PageRequest.of(0, 4);

        Mockito.when(orderRepository.findOrderByOrderStatusAndCustomer(any(OrderStatus.class),
                any(Customer.class), any(PageRequest.class))).thenReturn(ordersDTO.getActiveOrders());

        assertEquals(orderService.getOrdersDTO(userDetails.getUsername(), pageRequest).getActiveOrders().getNumberOfElements(),
                ordersDTO.getActiveOrders().getNumberOfElements());
    }

    @Test
    void updateOrder() {
        Order order = MockUtil.getOrder();
        CartOrderDTO cartOrderDTO = MockUtil.getCartOrderDTO();
        OrderDTO orderDTO = MockUtil.getOrderDTO();

        assertEquals(orderService.updateOrder(cartOrderDTO, order.getId()).getFoodOrders().size(),
                orderDTO.getFoodOrders().size());
    }

    @Test
    void deleteOrder() {
        Order order = MockUtil.getOrder();
        assertEquals(orderService.deleteOrder(order.getId()).getId(), order.getId());
    }

    @Test
    void locationToString() {
        Location mockLocation = MockUtil.getLocation();
        String finalString = "test, lane, something, United States";

        assertEquals(orderService.locationToString(mockLocation), finalString);
    }

    @Test
    void getOrdersDTO() {
        String username = MockUtil.getUserDetails().getUsername();
        PageRequest pageRequest = MockUtil.getPageRequest();
        OrdersDTO mockOrdersDTO = MockUtil.getOrdersDTO();
        OrdersDTO realOrdersDTO = orderService.getOrdersDTO(username, pageRequest);

        assertEquals(mockOrdersDTO.getActiveOrders().getTotalElements(),
                realOrdersDTO.getActiveOrders().getTotalElements());
    }

    @Test
    void getAvailableOrders() {
        List<Order> orders = List.of(MockUtil.getOrder());

        assertEquals(orderService.getAvailableOrders().size(), orders.size());
    }

    @Test
    void acceptOrder() {
        Order fakeOrder = MockUtil.getOrder();
        fakeOrder.setOrderStatus(OrderStatus.builder().status("AWAITING_DRIVER").build());
        String username = MockUtil.getUserDetails().getUsername();

        Mockito.when(orderRepository.findById(fakeOrder.getId()))
                .thenReturn(Optional.of(fakeOrder));

        Order realOrder = orderService.acceptOrder(username, fakeOrder.getId());
        assertEquals(realOrder.getId(), fakeOrder.getId());
    }

    @Test
    void abandonOrder() {
        String username = MockUtil.getUserDetails().getUsername();
        assertNull(orderService.abandonOrder(username).getDriver());
    }

    @Test
    void setPickedUpAt() {
        Order order = MockUtil.getOrder();
        order.setPickedUpAt(null);
        Order newOrder = orderService.setPickedUpAt(order.getId());

        assertNotNull(newOrder.getPickedUpAt());
    }

    @Test
    void fulfilOrder() {
        Order order = MockUtil.getOrder();
        Driver driver = orderService.fulfilOrder(order.getId()).getDriver();

        assertEquals(order.getDeliveryPay(), driver.getTotalPay());
    }

    @Test
    void getAcceptedOrder() {
        Order mockOrder = MockUtil.getOrder();
        Order order = orderService.getAcceptedOrder("");

        assertEquals(mockOrder.getId(), order.getId());
    }

    @Test
    void getDriverRating() {
        DriverRating driverRating = MockUtil.getDriverRating();
        Order order = MockUtil.getOrder();

        assertEquals(orderService.getDriverRating(order.getId()).getRating(),
                driverRating.getRating());
    }

    @Test
    void submitDriverRating() {
        RatingDTO ratingDTO = MockUtil.getRatingDTO();
        Order order = MockUtil.getOrder();

        DriverRating driverRating = orderService.submitDriverRating(order.getId(),
                ratingDTO);

        assertEquals(driverRating.getRating(), ratingDTO.getRating());
    }

    @Test
    void getPendingOrders() {
        String username = MockUtil.getUserDetails().getUsername();
        Integer amountOfOrders = orderService.getPendingOrders(username).size();

        assertEquals(amountOfOrders, 1);
    }
}