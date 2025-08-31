package com.ecommerce.services;

import com.ecommerce.models.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Async("notifications-pool")
    public void sendOrderConfirmation(Order savedOrder) {
        sendEmailConfirmation(savedOrder.getGuestEmail());

    }
    @Async("notifications-pool")
    public void sendOrderCancellation(Order savedOrder) {
    }
    @Async("notifications-pool")
    public void sendPaymentFailure(Order order, String reason) {
    }
    @Async("notifications-pool")
    public void sendOrderProcessing(Order order) {
    }
    @Async("notifications-pool")
    public void sendOrderShipped(Order order) {
    }
    @Async("notifications-pool")
    public void sendOrderDelivered(Order order) {
    }
    @Async("notifications-pool")
    public void sendOrderCompleted(Order order) {
    }



    private void sendEmailConfirmation(String guestEmail) {
    }
}
