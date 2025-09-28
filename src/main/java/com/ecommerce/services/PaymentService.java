package com.ecommerce.services;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.exceptions.PaymentFailedException;
import com.ecommerce.gateways.PaypalGateway;
import com.ecommerce.gateways.StripeGateway;
import com.ecommerce.models.*;
import com.ecommerce.repositories.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    // Gateways sandbox injectées
    private final StripeGateway stripeGateway;
    private final PaypalGateway paypalGateway;

    @Transactional
    public void processPayment(Payment payment, PaymentRequest paymentRequest) {
        log.info("Processing payment: reference={}, amount={}",
                payment.getPaymentReference(), payment.getAmount());

        try {
            validatePaymentRequest(paymentRequest, payment);
            switch (payment.getProvider()) {
                case STRIPE -> processStripePayment(payment, paymentRequest);
                case PAYPAL -> processPayPalPayment(payment, paymentRequest);
                default -> throw new PaymentFailedException("Unsupported payment provider: " + payment.getProvider());
            }
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(LocalDateTime.now());
            // Ne génère un ID que si la gateway ne l'a pas défini
            if (payment.getProviderTransactionId() == null) {
                payment.setProviderTransactionId(generateProviderTransactionId());
            }

            paymentRepository.save(payment);

            log.info("Payment processed successfully: reference={}", payment.getPaymentReference());

        } catch (Exception e) {
            // Payment failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            payment.setPaidAt(LocalDateTime.now());

            paymentRepository.save(payment);

            log.error("Payment failed: reference={}, reason={}",
                    payment.getPaymentReference(), e.getMessage());

            throw new PaymentFailedException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void processRefund(Payment payment) {
        log.info("Processing refund for payment: reference={}", payment.getPaymentReference());

        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Cannot refund payment that is not successful");
        }

        try {
            // Process refund based on provider
            switch (payment.getProvider()) {
                case STRIPE -> processStripeRefund(payment);
                case PAYPAL -> processPayPalRefund(payment);
                default -> throw new PaymentFailedException("Unsupported payment provider for refund: " + payment.getProvider());
            }

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            log.info("Refund processed successfully: reference={}", payment.getPaymentReference());

        } catch (Exception e) {
            log.error("Refund failed: reference={}, reason={}",
                    payment.getPaymentReference(), e.getMessage());
            throw new PaymentFailedException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    public Payment createPendingPayment(Order order, PaymentMethod method) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentReference(generatePaymentReference())
                .amount(BigDecimal.valueOf(order.getTotalAmount()))
                .currencyCode(order.getCurrencyCode())
                .method(method)
                .status(PaymentStatus.PENDING)
                .provider(determinePaymentProvider(method))
                .build();

        return paymentRepository.save(payment);
    }

    private void validatePaymentRequest(PaymentRequest request, Payment payment) {
        if (request.amount().compareTo(payment.getAmount()) != 0) {
            throw new PaymentFailedException("Payment amount mismatch");
        }

        if (!request.currencyCode().equals(payment.getCurrencyCode())) {
            throw new PaymentFailedException("Currency code mismatch");
        }
        switch (payment.getMethod()) {
            case CREDIT_CARD, DEBIT_CARD -> validateCardDetails(request);
            case PAYPAL -> validatePayPalDetails(request);
        }
    }

    private void processStripePayment(Payment payment, PaymentRequest request) {
        // Déléguer à la passerelle Stripe sandbox
        String providerTxnId = stripeGateway.charge(payment, request);
        payment.setProviderTransactionId(providerTxnId);
        log.info("Stripe payment processed for amount: {}", payment.getAmount());
    }

    private void processPayPalPayment(Payment payment, PaymentRequest request) {
        // Déléguer à la passerelle PayPal sandbox
        String providerTxnId = paypalGateway.charge(payment, request);
        payment.setProviderTransactionId(providerTxnId);
        log.info("PayPal payment processed for amount: {}", payment.getAmount());
    }

    private void processInternalPayment(Payment payment, PaymentRequest request) {
        // Internal payment processing (e.g., gift cards, store credit)
        log.info("Internal payment processed for amount: {}", payment.getAmount());
    }

    // ===== REFUND PROCESSING METHODS =====

    private void processStripeRefund(Payment payment) {
        stripeGateway.refund(payment);
        log.info("Stripe refund processed for payment: {}", payment.getPaymentReference());
    }

    private void processPayPalRefund(Payment payment) {
        paypalGateway.refund(payment);
        log.info("PayPal refund processed for payment: {}", payment.getPaymentReference());
    }

    private void processInternalRefund(Payment payment) {
        log.info("Internal refund processed for payment: {}", payment.getPaymentReference());
    }

    // ===== VALIDATION METHODS =====

    private void validateCardDetails(PaymentRequest request) {
        if (request.cardNumber() == null || request.cardNumber().length() < 13) {
            throw new PaymentFailedException("Invalid card number");
        }
        if (request.expiryMonth() == null || request.expiryYear() == null) {
            throw new PaymentFailedException("Card expiry date required");
        }
        if (request.cvv() == null || request.cvv().length() < 3) {
            throw new PaymentFailedException("Invalid CVV");
        }
    }

    private void validatePayPalDetails(PaymentRequest request) {
        if (request.paypalEmail() == null) {
            throw new PaymentFailedException("PayPal email required");
        }
    }

    private void validateBankDetails(PaymentRequest request) {
        if (request.bankAccount() == null) {
            throw new PaymentFailedException("Bank account details required");
        }
    }
    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String generateProviderTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateRefundReference() {
        return "REF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    private PaymentProvider determinePaymentProvider(PaymentMethod method) {
        return switch (method) {
            case CREDIT_CARD, DEBIT_CARD -> PaymentProvider.STRIPE;
            case PAYPAL -> PaymentProvider.PAYPAL;
        };
    }
}
