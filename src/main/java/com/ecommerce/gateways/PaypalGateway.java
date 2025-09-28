package com.ecommerce.gateways;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("PAYPAL")
public class PaypalGateway implements PaymentGateway {
    @Override
    public String charge(Payment payment, PaymentRequest request) {
        if (request.paypalEmail() == null || !request.paypalEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid PayPal email");
        }
        String txnId = "pp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        log.info("[PayPal sandbox] Charged {} {} -> txn {}", payment.getCurrencyCode(), payment.getAmount(), txnId);
        return txnId;
    }

    @Override
    public void refund(Payment payment) {
        log.info("[PayPal sandbox] Refunded payment {}", payment.getPaymentReference());
    }
}
