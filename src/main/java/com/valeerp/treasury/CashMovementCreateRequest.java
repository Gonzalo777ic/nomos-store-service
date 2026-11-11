package com.valeerp.treasury;

import java.math.BigDecimal;

public record CashMovementCreateRequest(
        String type,
        BigDecimal amount,
        String currency,
        String channel,
        Long paymentMethodId,
        Long cashRegisterId,
        Long cashSessionId,
        Long bankAccountId,
        String concept,
        String externalReference
) {
}
