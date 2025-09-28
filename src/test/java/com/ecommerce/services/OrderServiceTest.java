package com.ecommerce.services;

import com.ecommerce.exceptions.OrderNotFound;
import com.ecommerce.models.*;
import com.ecommerce.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests pour OrderService qui ne teste que les responsabilités CRUD.
 * L'orchestration est maintenant testée dans CheckoutServiceTest.
 */
public class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);

        // OrderService maintenant ne prend que OrderRepository
        orderService = new OrderService(orderRepository);

        // Configuration standard pour les mocks
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createOrder_withValidData_savesAndReturnsOrder() {
        String orderNumber = "ORD-123";
        Long userId = 42L;
        String shippingAddress = "123 Ship St";
        Double subtotal = 100.0;

        Order created = orderService.createOrder(
                orderNumber, userId, null, null,
                subtotal, 8.0, 5.0, 0.0,
                shippingAddress, "123 Bill St", "USD"
        );

        assertEquals(orderNumber, created.getOrderNumber());
        assertEquals(userId, created.getUserId());
        assertEquals(shippingAddress, created.getShippingAddress());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertEquals(113.0, created.getTotalAmount()); // 100 + 8 + 5 - 0
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void confirmOrder_setsStatusConfirmed_andSaves() {
        Long orderId = 5L;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order confirmed = orderService.confirmOrder(orderId);
        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void failOrder_setsStatusFailed_andSaves() {
        Long orderId = 6L;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order failed = orderService.failOrder(orderId, "Payment declined");
        assertEquals(OrderStatus.FAILED, failed.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_whenCancelable_setsStatusCanceledWithReasonAndTimestamp_andSaves() {
        Long orderId = 7L;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PENDING); // PENDING est annulable

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        String reason = "Customer request";
        Order canceled = orderService.cancelOrder(orderId, reason);

        assertEquals(OrderStatus.CANCELED, canceled.getStatus());
        assertEquals(reason, canceled.getCancelReason());
        assertNotNull(canceled.getCanceledAt());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_whenNotCancelable_throwsException() {
        Long orderId = 8L;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.COMPLETED); // COMPLETED n'est pas annulable

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(orderId, "Customer request"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_validTransition_updatesStatus() {
        Order order = new Order();
        order.setOrderId(5L);
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order updated = orderService.updateOrderStatus(5L, OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_invalidTransition_throwsException() {
        Order order = new Order();
        order.setOrderId(5L);
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        // PENDING -> SHIPPED est une transition invalide (doit passer par CONFIRMED et PROCESSING)
        assertThrows(IllegalStateException.class, () ->
            orderService.updateOrderStatus(5L, OrderStatus.SHIPPED));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void findOrderByNumber_notFound_throwsOrderNotFoundException() {
        when(orderRepository.findByOrderNumber("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(OrderNotFound.class, () ->
            orderService.findOrderByNumber("NONEXISTENT"));
    }

    @Test
    void findOrderById_notFound_throwsOrderNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFound.class, () ->
            orderService.findOrderById(999L));
    }

    @Test
    void getOrdersByUserId_returnsOrdersFromRepository() {
        Long userId = 42L;
        List<Order> expected = List.of(new Order(), new Order());
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(expected);

        List<Order> result = orderService.getOrdersByUserId(userId);

        assertSame(expected, result);
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getOrdersByStatus_returnsOrdersFromRepository() {
        OrderStatus status = OrderStatus.PENDING;
        List<Order> expected = List.of(new Order(), new Order());
        when(orderRepository.findByStatusOrderByCreatedAtDesc(status)).thenReturn(expected);

        List<Order> result = orderService.getOrdersByStatus(status);

        assertSame(expected, result);
        verify(orderRepository).findByStatusOrderByCreatedAtDesc(status);
    }
}
