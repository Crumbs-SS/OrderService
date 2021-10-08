package com.crumbs.orderservice.controller;

import com.crumbs.orderservice.MockUtil;
import com.crumbs.orderservice.service.CartService;
import com.crumbs.orderservice.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderService orderService;

    @MockBean
    CartService cartService;



    @Test
    void getCustomerOrders() throws Exception {
        mockMvc.perform(get("/order-service/customers/{username}/orders", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser()
    void getOrders() throws Exception {
        mockMvc.perform(get("/order-service/orders")
                .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder() throws Exception {
        mockMvc.perform(post("/order-service/customers/{username}/orders", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(MockUtil.getCartOrderDTO())))
                .andExpect(status().isCreated());
    }

    @Test
    void addToCart() throws Exception {
        mockMvc.perform(post("/order-service/customers/{username}/cart", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(MockUtil.getCartItemDTO())))
                .andExpect(status().isCreated());
    }

    @Test
    void UpdateOrder() throws Exception {
        mockMvc.perform(put("/order-service/orders/{orderId}",
                MockUtil.getOrder().getId())
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .header("Username", "correctUsername")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(MockUtil.getCartOrderDTO())))
                .andExpect(status().isNoContent());
    }


    @Test
    void getCartItems() throws Exception {
        mockMvc.perform(get("/order-service/customers/{username}/cart", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCart() throws Exception {
        mockMvc.perform(delete("/order-service/customers/{username}/cart", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .contentType("application/json"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeItem() throws Exception {
        mockMvc.perform(delete("/order-service/customers/{username}/cart/{cartId}",
                "correctUsername",
                MockUtil.getCartItem().getId())
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .contentType("application/json"))
                .andExpect(status().isOk());

    }



    @Test
    void deleteOrder() throws Exception {
        mockMvc.perform(delete("/order-service/orders/{id}", MockUtil.getOrder().getId())
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("ADMIN")))
                .contentType("application/json"))
                .andExpect(status().isOk());

    }
    @Test
    void getAvailableOrders() throws Exception {
        mockMvc.perform(get("/order-service/drivers/{username}/orders/available", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("DRIVER")))
                .contentType("application/json"))
                .andExpect(status().isOk());

    }
    @Test
    void getAcceptedOrder() throws Exception {
        mockMvc.perform(get("/order-service/drivers/{username}/accepted-order", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("DRIVER")))
                .contentType("application/json"))
                .andExpect(status().isOk());

    }
    @Test
    void acceptOrder() throws Exception {
        mockMvc.perform(post("/order-service/drivers/{username}/accepted-order", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("DRIVER")))
                .content(String.valueOf(MockUtil.getOrder().getId()))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    void setPickedUpAt() throws Exception {
        mockMvc.perform(put("/order-service/drivers/{username}/order/{orderId}/pickUp", "correctUsername", MockUtil.getOrder().getId())
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("DRIVER")))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    void fulfilOrder() throws Exception {
        mockMvc.perform(put("/order-service/drivers/{username}/order/{orderId}", "correctUsername", MockUtil.getOrder().getId())
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("DRIVER")))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    void getDriverRating() throws Exception {
        mockMvc.perform(get("/order-service/orders/{orderId}/driver-rating", MockUtil.getOrder().getId())
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("DRIVER")))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    void submitDriverRating() throws Exception {
        mockMvc.perform(post("/order-service/orders/{orderId}/driver-rating", MockUtil.getOrder().getId())
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("CUSTOMER")))
                .header("Username", "correctUsername")
                .content(objectMapper.writeValueAsString(MockUtil.getRatingDTO()))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    void abandonOrder() throws Exception {
        mockMvc.perform(delete("/order-service/drivers/{username}/accepted-order", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("DRIVER")))
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    void getPendingOrders() throws Exception {
        mockMvc.perform(get("/order-service/owners/{username}/restaurants/orders", "correctUsername")
                .header("Authorization", ("Bearer " + MockUtil.createMockJWT("OWNER")))
                .contentType("application/json"))
                .andExpect(status().isOk());

    }





}