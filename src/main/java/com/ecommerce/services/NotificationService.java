package com.ecommerce.services;

import com.ecommerce.models.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @Value("${ecommerce.notifications.mock:false}")
    private boolean mockMode;

    @Value("${ecommerce.notifications.enabled:true}")
    private boolean notificationsEnabled;

    @Async("notifications-pool")
    public void sendOrderConfirmation(Order savedOrder) {
        // in development or testing mode, we just log the notification instead of sending real emails
        if (!notificationsEnabled) {
            return;
        }

        if (mockMode) {
            log.info("MOCK NOTIFICATION: Order confirmation would be sent for order: {}, email: {}",
                    savedOrder.getOrderNumber(),
                    savedOrder.getUserId() != null ? "user-" + savedOrder.getUserId() : savedOrder.getGuestEmail());
            return;
        }

        sendEmailConfirmation(savedOrder.getGuestEmail());
    }

    @Async("notifications-pool")
    public void sendOrderCancellation(Order savedOrder) {
        if (!notificationsEnabled) {
            return;
        }

        if (mockMode) {
            log.info("MOCK NOTIFICATION: Order cancellation would be sent for order: {}",
                    savedOrder.getOrderNumber());
        }
        //
        //  i in production, we must send real emails
    }

    @Async("notifications-pool")
    public void sendPaymentFailure(Order order, String reason) {
        if (!notificationsEnabled) {
            return;
        }

        if (mockMode) {
            log.info("MOCK NOTIFICATION: Payment failure would be sent for order: {}, reason: {}",
                    order.getOrderNumber(), reason);
            return;
        }

        // Implémentation réelle ici
    }

    @Async("notifications-pool")
    public void sendOrderProcessing(Order order) {
        if (!notificationsEnabled) {
            return;
        }

        if (mockMode) {
            log.info("MOCK NOTIFICATION: Order processing would be sent for order: {}",
                    order.getOrderNumber());
            return;
        }

        // Implémentation réelle ici
    }

    @Async("notifications-pool")
    public void sendOrderShipped(Order order) {
        if (!notificationsEnabled) {
            return;
        }

        if (mockMode) {
            log.info("MOCK NOTIFICATION: Order shipped would be sent for order: {}",
                    order.getOrderNumber());
            return;
        }

        // Implémentation réelle ici
    }

    @Async("notifications-pool")
    public void sendOrderDelivered(Order order) {
        if (!notificationsEnabled) {
            return;
        }

        if (mockMode) {
            log.info("MOCK NOTIFICATION: Order delivered would be sent for order: {}",
                    order.getOrderNumber());
            return;
        }

        // Implémentation réelle ici
    }

    @Async("notifications-pool")
    public void sendOrderCompleted(Order order) {
        if (!notificationsEnabled) {
            return;
        }

        if (mockMode) {
            log.info("MOCK NOTIFICATION: Order completed would be sent for order: {}",
                    order.getOrderNumber());
            return;
        }

        // Implémentation réelle ici
    }

    void sendEmailConfirmation(String guestEmail) {
        // Implémentation réelle de l'envoi d'email pour la production
        log.info("Sending real email confirmation to: {}", guestEmail);
    }
}

