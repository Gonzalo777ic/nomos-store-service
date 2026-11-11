package com.valeerp.sales;

import java.math.BigDecimal;
import java.time.Instant;

public record SaleOrderUpdateRequest(
        Instant validUntil,
        String invoicingPolicy,
        String notes,
        String globalDiscountKind,
        BigDecimal globalDiscountValue,
        Long sellerId,
        Long salesTeamId,
        Long territoryId,
        Long commissionRuleId
) {
}
