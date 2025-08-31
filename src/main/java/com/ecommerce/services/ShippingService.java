package com.ecommerce.services;

import com.ecommerce.DTOs.CreateOrderRequest;
import com.ecommerce.models.*;
import com.ecommerce.repositories.ShipmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class ShippingService {
    private static final Logger logger = Logger.getLogger(ShippingService.class.getName());
    private final ShipmentRepository shipmentRepository;

    /**
     * Updates existing shipment to SHIPPED status when order status changes to SHIPPED
     */
    @Transactional
    public void createShipment(Order order) {
        Shipment shipment = order.getShipment();

        if (shipment == null) {
            throw new IllegalStateException("No pending shipment found for order: " + order.getOrderNumber());
        }

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            throw new IllegalStateException("Shipment is not in pending status for order: " + order.getOrderNumber());
        }

        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(LocalDateTime.now());
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setProvider(determineShippingProvider(order));
        shipment.setServiceName(determineServiceName(order));
        shipment.setPickupAddress(getWarehouseAddress());

        shipmentRepository.save(shipment);

        logger.info("Shipment created for order " + order.getOrderNumber() +
                ", tracking: " + shipment.getTrackingNumber());
    }

    /**
     * Creates initial pending shipment during order creation
     */
    @Transactional
    public Shipment createPendingShipment(Order order, CreateOrderRequest request) {
        Shipment shipment = Shipment.builder()
                .order(order)
                .status(ShipmentStatus.PENDING)
                .deliveryAddress(order.getShippingAddress())
                .shippingCost(order.getShippingCost())
                .deliveryInstructions(request.getDeliveryInstructions())
                .estimatedDeliveryDate(calculateEstimatedDelivery(request.getExpressShipping()))
                .serviceName(determineServiceNameFromRequest(request))
                .build();

        return shipmentRepository.save(shipment);
    }

    /**
     * Mark shipment as failed (you can add this status)
     */
    @Transactional
    public void failShipment(Order order, String reason) {
        Shipment shipment = order.getShipment();

        if (shipment == null) {
            throw new IllegalStateException("No shipment found for order: " + order.getOrderNumber());
        }

        shipment.setStatus(ShipmentStatus.FAILED);
        shipment.setNotes("Shipment failed: " + reason);

        shipmentRepository.save(shipment);

        logger.warning("Shipment failed for order " + order.getOrderNumber() + ": " + reason);
    }

    /**
     * Update shipment when delivered
     */
    @Transactional
    public void markAsDelivered(Order order) {
        Shipment shipment = order.getShipment();

        if (shipment == null || shipment.getStatus() != ShipmentStatus.SHIPPED) {
            throw new IllegalStateException("Cannot mark as delivered - invalid shipment status");
        }

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDeliveryDate(LocalDateTime.now());

        shipmentRepository.save(shipment);

        logger.info("Shipment delivered for order " + order.getOrderNumber());
    }

    // ===== HELPER METHODS =====

    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * Determine shipping provider based on service level and cost
     */
    private ShippingProvider determineShippingProvider(Order order) {
        // Logic to determine provider based on shipping cost, address, etc.
        if (order.getShippingCost() > 10.0) {
            return ShippingProvider.FEDEX;
        } else {
            return ShippingProvider.UPS;
        }
    }

    /**
     * Determine service name based on order
     */
    private String determineServiceName(Order order) {
        return order.getShippingCost() > 10.0 ? "Express Delivery" : "Standard Delivery";
    }

    /**
     * Determine service name from request during order creation
     */
    private String determineServiceNameFromRequest(CreateOrderRequest request) {
        return Boolean.TRUE.equals(request.getExpressShipping()) ? "Express Delivery" : "Standard Delivery";
    }

    /**
     * Calculate estimated delivery date
     */
    private LocalDateTime calculateEstimatedDelivery(Boolean expressShipping) {
        int daysToAdd = Boolean.TRUE.equals(expressShipping) ? 2 : 5;
        return LocalDateTime.now().plusDays(daysToAdd);
    }

    /**
     * Get warehouse/pickup address
     */
    private String getWarehouseAddress() {
        // In production, this could come from configuration or database
        return "Main Warehouse, 123 Distribution Center Dr, Warehouse City, WC 12345";
    }
}