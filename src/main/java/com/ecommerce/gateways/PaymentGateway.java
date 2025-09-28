package com.ecommerce.gateways;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.Payment;

public interface PaymentGateway {
    /**
     * Effectue un débit/encaissement et retourne un identifiant transaction fournisseur
     */
    String charge(Payment payment, PaymentRequest request);

    /**
     * Effectue un remboursement (peut retourner un id, ici on n'en a pas besoin)
     */
    void refund(Payment payment);
}
