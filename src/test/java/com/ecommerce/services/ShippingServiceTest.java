package com.ecommerce.services;

import com.ecommerce.models.*;
import com.ecommerce.repositories.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ShippingServiceTest {

    private ShipmentRepository shipmentRepository;
    private ShippingService shippingService;

    @BeforeEach
    void setup() {
        shipmentRepository = mock(ShipmentRepository.class);
        shippingService = new ShippingService(shipmentRepository);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createShipment_whenPending_setsShippedAndTracking() {
        Order order = new Order();
        order.setOrderNumber("ORD-1");
        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);
        order.setShipment(shipment);
        order.setShippingCost(5.0);

        shippingService.createShipment(order);

        assertEquals(ShipmentStatus.SHIPPED, shipment.getStatus());
        assertNotNull(shipment.getTrackingNumber());
        assertNotNull(shipment.getShippedAt());
        assertNotNull(shipment.getProvider());
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void createShipment_whenNoShipment_throws() {
        Order order = new Order();
        order.setOrderNumber("ORD-2");
        assertThrows(IllegalStateException.class, () -> shippingService.createShipment(order));
        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void failShipment_setsFailedAndSaves() {
        Order order = new Order();
        order.setOrderNumber("ORD-3");
        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);
        order.setShipment(shipment);

        shippingService.failShipment(order, "Payment failed");

        assertEquals(ShipmentStatus.FAILED, shipment.getStatus());
        assertTrue(shipment.getNotes().contains("Payment failed"));
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void markAsDelivered_whenShipped_setsDelivered() {
        Order order = new Order();
        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.SHIPPED);
        order.setShipment(shipment);

        shippingService.markAsDelivered(order);

        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        assertNotNull(shipment.getActualDeliveryDate());
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void markAsDelivered_whenInvalidStatus_throws() {
        Order order = new Order();
        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);
        order.setShipment(shipment);

        assertThrows(IllegalStateException.class, () -> shippingService.markAsDelivered(order));
        verifyNoMoreInteractions(shipmentRepository);
    }
}
