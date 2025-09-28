package com.ecommerce.services;

import com.ecommerce.models.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

class NotificationServiceTest {
    private NotificationService notificationService;
    private Order order;

    @BeforeEach
    void setUp() {
        notificationService = Mockito.spy(new NotificationService());
        order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn("ORD-123");
        when(order.getUserId()).thenReturn(42L);
        when(order.getGuestEmail()).thenReturn("guest@example.com");
    }

    @Test
    void sendOrderConfirmation_disabled_doesNothing() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", false);
        notificationService.sendOrderConfirmation(order);
        verify(notificationService, never()).sendEmailConfirmation(anyString());
    }

    @Test
    void sendOrderConfirmation_mockMode_logsOnly() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", true);
        notificationService.sendOrderConfirmation(order);
        verify(notificationService, never()).sendEmailConfirmation(anyString());
    }

    @Test
    void sendOrderConfirmation_realMode_sendsEmail() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", false);
        doNothing().when(notificationService).sendEmailConfirmation(anyString());
        notificationService.sendOrderConfirmation(order);
        verify(notificationService, times(1)).sendEmailConfirmation("guest@example.com");
    }

    @Test
    void sendOrderCancellation_disabled_doesNothing() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", false);
        notificationService.sendOrderCancellation(order);
        // nothing to verify, just ensure no exception
    }

    @Test
    void sendOrderCancellation_mockMode_logsOnly() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", true);
        notificationService.sendOrderCancellation(order);
        // nothing to verify, just ensure no exception
    }

    @Test
    void sendOrderCancellation_realMode_noException() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", false);
        notificationService.sendOrderCancellation(order);
        // nothing to verify, just ensure no exception
    }

    @Test
    void sendPaymentFailure_disabled_doesNothing() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", false);
        notificationService.sendPaymentFailure(order, "Insufficient funds");
    }

    @Test
    void sendPaymentFailure_mockMode_logsOnly() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", true);
        notificationService.sendPaymentFailure(order, "Insufficient funds");
    }

    @Test
    void sendPaymentFailure_realMode_noException() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", false);
        notificationService.sendPaymentFailure(order, "Insufficient funds");
    }

    @Test
    void sendOrderProcessing_disabled_doesNothing() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", false);
        notificationService.sendOrderProcessing(order);
    }

    @Test
    void sendOrderProcessing_mockMode_logsOnly() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", true);
        notificationService.sendOrderProcessing(order);
    }

    @Test
    void sendOrderProcessing_realMode_noException() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", false);
        notificationService.sendOrderProcessing(order);
    }

    @Test
    void sendOrderShipped_disabled_doesNothing() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", false);
        notificationService.sendOrderShipped(order);
    }

    @Test
    void sendOrderShipped_mockMode_logsOnly() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", true);
        notificationService.sendOrderShipped(order);
    }

    @Test
    void sendOrderShipped_realMode_noException() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", false);
        notificationService.sendOrderShipped(order);
    }

    @Test
    void sendOrderDelivered_disabled_doesNothing() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", false);
        notificationService.sendOrderDelivered(order);
    }

    @Test
    void sendOrderDelivered_mockMode_logsOnly() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", true);
        notificationService.sendOrderDelivered(order);
    }

    @Test
    void sendOrderDelivered_realMode_noException() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", false);
        notificationService.sendOrderDelivered(order);
    }

    @Test
    void sendOrderCompleted_disabled_doesNothing() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", false);
        notificationService.sendOrderCompleted(order);
    }

    @Test
    void sendOrderCompleted_mockMode_logsOnly() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", true);
        notificationService.sendOrderCompleted(order);
    }

    @Test
    void sendOrderCompleted_realMode_noException() {
        ReflectionTestUtils.setField(notificationService, "notificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "mockMode", false);
        notificationService.sendOrderCompleted(order);
    }
}
