package com.ecommerce.controllers;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.*;
import com.ecommerce.services.CheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import com.ecommerce.configurations.JwtAuthenticationFilter;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CheckoutController.class)
class CheckoutControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CheckoutService checkoutService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private Authentication authWithUser(Long userId) {
        User user = User.builder()
                .userId(userId)
                .email("user@test.com")
                .password("pwd")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        SecurityUser su = new SecurityUser(user);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(su, null, authorities);
    }

    @BeforeEach
    void setup() {
    }

    @Test
    void createOrder_returnsCreatedOrder() throws Exception {
        CreateOrderRequest req = CreateOrderRequest.builder()
                .shippingAddress("Ship")
                .billingAddress("Bill")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
        Order order = new Order();
        order.setOrderNumber("ORD-xyz");
        Mockito.when(checkoutService.createOrderForUser(eq(42L), any(CreateOrderRequest.class)))
                .thenReturn(order);

        mockMvc.perform(post("/api/checkout/orders")
                        .with(authentication(authWithUser(42L)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void processPayment_returnsOkWithOrder() throws Exception {
        PaymentRequest pr = new PaymentRequest(BigDecimal.valueOf(100), "USD", "4242", "12", "2030", "123", null, null);
        Order order = new Order();
        order.setOrderNumber("ORD-pay");

        Mockito.when(checkoutService.processPaymentByOrderId(eq(100L), any(PaymentRequest.class)))
                .thenReturn(order);

        mockMvc.perform(post("/api/checkout/orders/100/payment")
                        .with(authentication(authWithUser(42L)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pr)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_returnsOk() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-cancel");
        Mockito.when(checkoutService.cancelOrder(100L, "Cancelled by user"))
                .thenReturn(order);

        mockMvc.perform(post("/api/checkout/orders/100/cancel")
                        .with(authentication(authWithUser(42L)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
