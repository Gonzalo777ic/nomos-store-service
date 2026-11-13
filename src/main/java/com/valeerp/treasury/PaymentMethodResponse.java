package com.valeerp.treasury;

import com.valeerp.store.service.model.PaymentMethodConfig;

public record PaymentMethodResponse(
        Long id,
        String code,
        String name,
        String kind,
        String kindLabel,
        boolean requiresReference
) {

    public static PaymentMethodResponse from(PaymentMethodConfig method) {
        return new PaymentMethodResponse(
                method.getId(),
                method.getCode(),
                method.getName(),
                method.getKind().name(),
                method.getKind().label(),
                method.isRequiresReference()
        );
    }
}
