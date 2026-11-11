package com.valeerp.sales;

import java.time.Instant;

public record SaleOrderCreateRequest(
        Long partnerId,
        Long opportunityId,
        String currency,
        Instant validUntil,
        String invoicingPolicy,
        String notes
) {
}
