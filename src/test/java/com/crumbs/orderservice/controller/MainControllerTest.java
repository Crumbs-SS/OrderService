package com.crumbs.orderservice.controller;

import com.crumbs.orderservice.MockUtil;
import com.crumbs.orderservice.service.CartService;
import com.crumbs.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    void getOrders() throws Exception {
        mockMvc.perform(get("/customers/{id}/orders", MockUtil.getCustomer().getId())
                .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder() throws Exception {
        mockMvc.perform(post("/customers/{id}/orders", MockUtil.getCustomer().getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(MockUtil.getCartOrderDTO())))
                .andExpect(status().isCreated());
    }

    @Test
    void addToCart() throws Exception {
        mockMvc.perform(post("/customers/{id}/cart", MockUtil.getCustomer().getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(MockUtil.getCartItemDTO())))
                .andExpect(status().isCreated());
    }

    @Test
    void updateOrder() throws Exception {
        mockMvc.perform(put("/customers/{customerId}/order/{orderId}",
                MockUtil.getCustomer().getId(),
                MockUtil.getOrder().getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(MockUtil.getCartOrderDTO())))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCartItems() throws Exception {
        mockMvc.perform(get("/customers/{id}/cart", MockUtil.getCustomer().getId())
                .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCart() throws Exception {
        mockMvc.perform(delete("/customers/{id}/cart", MockUtil.getCustomer().getId())
                .contentType("application/json"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeItem() throws Exception {
        mockMvc.perform(delete("/customers/{userId}/cart/{cartId}",
                MockUtil.getCustomer().getId(),
                MockUtil.getCartItem().getId())
                .contentType("application/json"))
                .andExpect(status().isOk());

    }
}