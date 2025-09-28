package com.ecommerce.services;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.exceptions.OrderNotFound;
import com.ecommerce.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CheckoutServiceTest {

    private OrderService orderService;
    private CartService cartService;
    private StockService stockService;
    private PaymentService paymentService;
    private ShippingService shippingService;
    private NotificationService notificationService;

    private CheckoutService checkoutService;

    @BeforeEach
    void setup() {
        orderService = mock(OrderService.class);
        cartService = mock(CartService.class);
        stockService = mock(StockService.class);
        paymentService = mock(PaymentService.class);
        shippingService = mock(ShippingService.class);
        notificationService = mock(NotificationService.class);

        checkoutService = new CheckoutService(
                orderService, cartService, stockService,
                paymentService, shippingService, notificationService);
    }

    @Test
    void createOrderFromCart_orchestratesAllServices_andReturnsCompleteOrder() {
        // Arrange
        Cart cart = new Cart();
        cart.setTotalAmount(100.0);
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPrice(20.0);
        cart.addItem(item);

        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("Ship Address")
                .billingAddress("Bill Address")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .expressShipping(true)
                .build();

        List<StockReservation> reservations = List.of(new StockReservation());
        when(stockService.reserveStockForCart(cart)).thenReturn(reservations);

        // Important: simuler la création d'Order avec OrderService
        Order order = new Order();
        order.setOrderId(1L);
        order.setOrderNumber("ORD-123");
        order.setStatus(OrderStatus.PENDING);
        when(orderService.createOrder(
                anyString(), eq(10L), isNull(), isNull(),
                eq(100.0), anyDouble(), anyDouble(), anyDouble(),
                eq("Ship Address"), eq("Bill Address"), eq("USD")
        )).thenReturn(order);

        Payment payment = new Payment();
        when(paymentService.createPendingPayment(order, PaymentMethod.CREDIT_CARD)).thenReturn(payment);

        Shipment shipment = new Shipment();
        when(shippingService.createPendingShipment(eq(order), any())).thenReturn(shipment);

        // NOUVEAU: Mock pour la méthode saveOrder ajoutée
        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setOrderNumber("ORD-123");
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setPayment(payment);
        savedOrder.setShipment(shipment);
        when(orderService.saveOrder(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = checkoutService.createOrderFromCart(cart, request, 10L, null, null);

        // Assert
        assertEquals("ORD-123", result.getOrderNumber());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertSame(payment, result.getPayment());
        assertSame(shipment, result.getShipment());

        // Verify service interactions
        verify(stockService).reserveStockForCart(cart);
        verify(orderService).createOrder(anyString(), eq(10L), isNull(), isNull(),
                eq(100.0), anyDouble(), anyDouble(), anyDouble(),
                eq("Ship Address"), eq("Bill Address"), eq("USD"));
        verify(paymentService).createPendingPayment(order, PaymentMethod.CREDIT_CARD);
        verify(shippingService).createPendingShipment(eq(order), any());
        // NOUVEAU: Vérifier que saveOrder est appelé
        verify(orderService).saveOrder(any(Order.class));
    }

    @Test
    void createOrderFromCart_onException_releasesReservations() {
        // Arrange
        Cart cart = new Cart();
        List<StockReservation> reservations = List.of(new StockReservation());
        when(stockService.reserveStockForCart(cart)).thenReturn(reservations);

        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("Ship")
                .billingAddress("Bill")
                .build();

        // OrderService lance une exception
        when(orderService.createOrder(
                anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("Test error"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> checkoutService.createOrderFromCart(cart, request, 10L, null, null));

        // Verify rollback
        verify(stockService).releaseReservations(reservations);
    }

    @Test
    void processPaymentByOrderId_onSuccessfulPayment_confirmsOrder_and_clearsCart() {
        // Arrange
        Order order = new Order();
        order.setOrderId(5L);
        order.setOrderNumber("ORD-5");
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(42L);

        // Créer des réservations réelles au lieu d'une liste vide
        StockReservation reservation1 = new StockReservation();
        reservation1.setProductId(1L);
        reservation1.setQuantity(2);
        reservation1.setStatus(ReservationStatus.ACTIVE);

        StockReservation reservation2 = new StockReservation();
        reservation2.setProductId(2L);
        reservation2.setQuantity(1);
        reservation2.setStatus(ReservationStatus.ACTIVE);

        List<StockReservation> reservations = List.of(reservation1, reservation2);
        order.setStockReservations(reservations);

        Payment payment = new Payment();
        order.setPayment(payment);

        when(orderService.findOrderById(5L)).thenReturn(order);
        when(orderService.confirmOrder(5L)).thenReturn(order);

        PaymentRequest paymentRequest = new PaymentRequest(
                BigDecimal.valueOf(100), "USD", "4242", "12", "2030", "123", null, null);

        // Simuler un paiement réussi
        doAnswer(inv -> {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(LocalDateTime.now());
            return null;
        }).when(paymentService).processPayment(payment, paymentRequest);

        // Act
        Order result = checkoutService.processPaymentByOrderId(5L, paymentRequest);

        // Assert
        verify(paymentService).processPayment(payment, paymentRequest);
        verify(orderService).confirmOrder(5L);
        verify(stockService).confirmReservations(reservations); // Maintenant ça devrait passer
        verify(cartService).clearUserCart(42L);
        verify(notificationService).sendOrderConfirmation(order);
    }

    @Test
    void processPaymentByOrderId_onFailedPayment_marksOrderAsFailed_and_releasesReservations() {
        // Arrange
        Order order = new Order();
        order.setOrderId(5L);
        order.setOrderNumber("ORD-5");
        order.setStatus(OrderStatus.PENDING);

        List<StockReservation> reservations = new ArrayList<>();
        order.setStockReservations(reservations);

        Payment payment = new Payment();
        order.setPayment(payment);

        Shipment shipment = new Shipment();
        order.setShipment(shipment);

        when(orderService.findOrderById(5L)).thenReturn(order);

        PaymentRequest paymentRequest = new PaymentRequest(
                BigDecimal.valueOf(100), "USD", "4000", "12", "2030", "123", null, null);

        // Simuler un paiement échoué
        String failureReason = "Card declined";
        doAnswer(inv -> {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(failureReason);
            return null;
        }).when(paymentService).processPayment(payment, paymentRequest);

        // Act
        Order result = checkoutService.processPaymentByOrderId(5L, paymentRequest);

        // Assert
        verify(paymentService).processPayment(payment, paymentRequest);
        verify(orderService).failOrder(5L, failureReason);
        verify(stockService).releaseReservations(reservations);
        verify(shippingService).failShipment(eq(order), contains("Payment failed"));
        verify(notificationService).sendPaymentFailure(order, failureReason);
    }

    @Test
    void cancelOrder_performsFullCancellation_with_allServices() {
        // Arrange
        Order order = new Order();
        order.setOrderId(7L);
        order.setOrderNumber("ORD-7");
        order.setStatus(OrderStatus.PENDING);

        List<StockReservation> reservations = new ArrayList<>();
        order.setStockReservations(reservations);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCEEDED);
        order.setPayment(payment);

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);
        order.setShipment(shipment);

        when(orderService.findOrderById(7L)).thenReturn(order);

        Order canceledOrder = new Order();
        canceledOrder.setStatus(OrderStatus.CANCELED);
        when(orderService.cancelOrder(eq(7L), anyString())).thenReturn(canceledOrder);

        // Act
        Order result = checkoutService.cancelOrder(7L, "Customer request");

        // Assert
        assertEquals(OrderStatus.CANCELED, result.getStatus());
        verify(stockService).releaseReservations(reservations);
        verify(paymentService).processRefund(payment);
        verify(shippingService).failShipment(eq(order), contains("Order canceled"));
        verify(orderService).cancelOrder(eq(7L), anyString());
        verify(notificationService).sendOrderCancellation(canceledOrder);
    }
}
