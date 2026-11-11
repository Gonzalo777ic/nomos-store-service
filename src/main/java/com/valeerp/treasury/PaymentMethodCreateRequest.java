package com.valeerp.treasury;

public record PaymentMethodCreateRequest(
        String code,
        String name,
        String kind,
        Boolean requiresReference
) {
}
