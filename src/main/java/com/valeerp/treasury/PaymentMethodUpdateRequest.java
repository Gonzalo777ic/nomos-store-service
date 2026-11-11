package com.valeerp.treasury;

public record PaymentMethodUpdateRequest(
        String code,
        String name,
        String kind,
        Boolean requiresReference
) {
}
