package com.ecommerce.factories;

import com.ecommerce.gateways.PaymentGateway;

public interface PaymentGatewayFactory {
    PaymentGateway getPaymentGateway(String method);
}

