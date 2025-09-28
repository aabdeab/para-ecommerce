package com.ecommerce.gateways;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("STRIPE")
public class StripeGateway implements PaymentGateway{
    @Override
    public String charge(Payment payment, PaymentRequest request) {
        // Simulation Stripe sandbox: 4242... => succès, 400000... => échecs divers
        String card = request.cardNumber();
        if (card == null || card.length() < 13) {
            throw new IllegalArgumentException("Invalid card number");
        }
        if (card.startsWith("400000")) {
            throw new RuntimeException("Stripe sandbox: card declined");
        }
        String txnId = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        log.info("[Stripe sandbox] Charged {} {} -> txn {}", payment.getCurrencyCode(), payment.getAmount(), txnId);
        return txnId;
    }

    @Override
    public void refund(Payment payment) {
        log.info("[Stripe sandbox] Refunded payment {}", payment.getPaymentReference());
    }
}
