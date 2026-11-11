package com.valeerp.treasury;

import java.math.BigDecimal;

public record CashSessionOpenRequest(
        Long cashRegisterId,
        BigDecimal openingAmount,
        String currency
) {
}
