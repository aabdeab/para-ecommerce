package com.ecommerce.services;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.gateways.PaypalGateway;
import com.ecommerce.gateways.StripeGateway;
import com.ecommerce.models.*;
import com.ecommerce.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentServiceStripeTest {

    private PaymentRepository paymentRepository;
    private StripeGateway stripeGateway;
    private PaypalGateway paypalGateway;

    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        paymentRepository = mock(PaymentRepository.class);
        stripeGateway = mock(StripeGateway.class);
        paypalGateway = mock(PaypalGateway.class);
        paymentService = new PaymentService(paymentRepository, stripeGateway, paypalGateway);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Payment newStripePendingPayment() {
        Order dummyOrder = new Order();
        return Payment.builder()
                .order(dummyOrder)
                .paymentReference("PAY-TEST-123")
                .amount(BigDecimal.valueOf(100))
                .currencyCode("USD")
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .provider(PaymentProvider.STRIPE)
                .build();
    }

    @Test
    void processPayment_stripe_success_setsSucceededAndTransactionId() {
        Payment payment = newStripePendingPayment();
        PaymentRequest req = new PaymentRequest(
                BigDecimal.valueOf(100),
                "USD",
                "4242424242424242",
                "12",
                "2030",
                "123",
                null,
                null
        );

        when(stripeGateway.charge(any(Payment.class), any(PaymentRequest.class)))
                .thenReturn("pi_test_txn_123");

        paymentService.processPayment(payment, req);

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertNotNull(payment.getPaidAt());
        assertEquals("pi_test_txn_123", payment.getProviderTransactionId());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, atLeastOnce()).save(captor.capture());
        assertEquals(PaymentStatus.SUCCEEDED, captor.getValue().getStatus());
    }

    @Test
    void processPayment_stripe_declined_setsFailedAndThrows() {
        Payment payment = newStripePendingPayment();
        PaymentRequest req = new PaymentRequest(
                BigDecimal.valueOf(100),
                "USD",
                "4000000000000002", // carte test déclinée
                "12",
                "2030",
                "123",
                null,
                null
        );

        when(stripeGateway.charge(any(Payment.class), any(PaymentRequest.class)))
                .thenThrow(new RuntimeException("Stripe sandbox: card declined"));

        Exception ex = assertThrows(RuntimeException.class, () -> paymentService.processPayment(payment, req));
        assertTrue(ex.getMessage().contains("Payment processing failed"));

        // Après l'échec, l'état du paiement doit être FAILED et la raison renseignée
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("Stripe sandbox: card declined", payment.getFailureReason());

        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    void processRefund_stripe_succeeded_setsRefunded() {
        Payment payment = newStripePendingPayment();
        payment.setStatus(PaymentStatus.SUCCEEDED);

        doNothing().when(stripeGateway).refund(any(Payment.class));

        paymentService.processRefund(payment);

        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertNotNull(payment.getRefundedAt());
        verify(stripeGateway, times(1)).refund(payment);
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }
}

