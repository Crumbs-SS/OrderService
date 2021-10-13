package com.crumbs.orderservice.service;

import com.crumbs.lib.entity.*;
import com.crumbs.lib.repository.*;
import com.crumbs.orderservice.MockUtil;
import com.crumbs.orderservice.dto.CartOrderDTO;
import com.crumbs.orderservice.dto.OrderDTO;
import com.crumbs.orderservice.dto.OrdersDTO;
import com.crumbs.orderservice.dto.RatingDTO;
import com.crumbs.orderservice.mapper.OrderDTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

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

        when(userDetailsRepository.findById(userDetails.getId()))
                .thenReturn(Optional.of(userDetails));
        when(userDetailsRepository.findByUsername(userDetails.getUsername()))
                .thenReturn(Optional.of(userDetails));
        when(locationRepository.save(any(Location.class)))
                .thenReturn(null);
        when(restaurantRepository.findById(restaurant.getId()))
                .thenReturn(Optional.of(restaurant));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(MockUtil.getOrder());
        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(orderDTOMapper.getOrderDTO(order))
                .thenReturn(orderDTO);
        when(orderStatusRepository.save(orderStatus))
                .thenReturn(orderStatus);
        when(orderStatusRepository.findById(any(String.class)))
                .thenReturn(Optional.of(orderStatus));
        when(driverStateRepository.findById(any(String.class)))
                .thenReturn(Optional.of(DriverState.builder().build()));
        when(userDetailsRepository.save(any(UserDetails
                .class))).thenReturn(userDetails);
        when(orderRepository.findDriverAcceptedOrder(any(String.class)))
                .thenReturn(List.of(order));
        when(driverRepository.save(any(Driver.class)))
                .thenReturn(MockUtil.getDriver());
        when(orderRepository.findOrderByOrderStatus(any(OrderStatus.class)))
                .thenReturn(List.of(order));
        when(driverRatingRepository.findDriverRatingByOrderId(order.getId()))
                .thenReturn(MockUtil.getDriverRating());
        when(driverRatingRepository.save(any(DriverRating.class)))
                .thenReturn(MockUtil.getDriverRating());
        when(orderRepository
                .findOrderByOrderStatusAndCustomer(
                        any(OrderStatus.class),
                        any(Customer.class),
                        any(PageRequest.class)))
                .thenReturn(orderPage);
        when(orderRepository.findRestaurantPendingOrders(restaurant.getId()))
                .thenReturn(List.of(order));
        when(menuItemRepository.findById(menuItem.getId()))
                .thenReturn(Optional.of(menuItem));

        doNothing().when(foodOrderRepository).delete(any(FoodOrder.class));
        when(paymentRepository.findPaymentByStripeID(payment.getStripeID()))
                .thenReturn(payment);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);
        when(orderRepository.findAll(
                ArgumentMatchers.<Specification<Order>>any(), any(Pageable.class)))
                .thenReturn(MockUtil.getOrders());
    }

    @Test
    void createOrder() {
        String username = MockUtil.getUserDetails().getUsername();
        CartOrderDTO cartOrderDTO = MockUtil.getCartOrderDTO();

        assertEquals(1, orderService.createOrder(username, cartOrderDTO).size());
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
    void shouldReturnOrdersPaginated(){
        assertEquals(1, orderService.getOrders("orders","",
                PageRequest.of(1, 2)).getTotalElements());
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

        assertEquals(1, amountOfOrders);
    }

    @Test
    void itShouldThrowRuntimeExceptionForOrder(){
        Order fakeOrder = MockUtil.getOrder();
        String username = MockUtil.getUserDetails().getUsername();
        fakeOrder.setOrderStatus(OrderStatus.builder().status("FULFILLED").build());

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(fakeOrder));

        assertThrows(RuntimeException.class, () -> orderService.acceptOrder(username,
                fakeOrder.getId()));
    }

    @Test
    void itShouldThrowRuntimeExceptionForDriver(){
        Order fakeOrder = MockUtil.getOrder();
        String username = MockUtil.getUserDetails().getUsername();
        fakeOrder.setOrderStatus(OrderStatus.builder().status("AWAITING_DRIVER").build());

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(fakeOrder));

        when(orderRepository.findDriverAcceptedOrder(anyString()))
                .thenReturn(List.of(fakeOrder, fakeOrder));

        assertThrows(RuntimeException.class, () -> orderService.acceptOrder(username,
                fakeOrder.getId()));
    }

    @Test
    void itShouldSetDriverPayToOrderPay(){
        Order order = MockUtil.getOrder();

        Driver driverWithNoAdditionalPay = Driver.builder().build();
        order.setDriver(driverWithNoAdditionalPay);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        Driver driver = orderService.fulfilOrder(order.getId()).getDriver();
        assertEquals(order.getDeliveryPay(), driver.getTotalPay());
    }
}