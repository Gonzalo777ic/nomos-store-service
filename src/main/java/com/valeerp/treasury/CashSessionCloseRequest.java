package com.valeerp.treasury;

import java.math.BigDecimal;

public record CashSessionCloseRequest(
        BigDecimal countedAmount,
        String currency,
        String differenceNote
) {
}
