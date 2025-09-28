package com.ecommerce.factories;

import com.ecommerce.gateways.PaymentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

@Component
public class PaymentMethodFactory implements PaymentGatewayFactory {
    private final Map<String, PaymentGateway> gatewayMap;

    @Autowired
    public PaymentMethodFactory(Map<String, PaymentGateway> gatewayMap) {
        this.gatewayMap = gatewayMap;
    }

    /**
     * Retourne le PaymentGateway approprié selon le nom de la méthode de paiement.
     *
     * @param method nom de la méthode (ex: "PAYPAL", "STRIPE")
     * @return PaymentGateway correspondant
     * @throws IllegalArgumentException si la méthode n'est pas supportée
     */
    @Override
    public PaymentGateway getPaymentGateway(String method) {
        Assert.hasText(method, "Payment method must be provided and not empty");
        PaymentGateway gateway = gatewayMap.get(method.toUpperCase());
        if (gateway == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
        return gateway;
    }
}
