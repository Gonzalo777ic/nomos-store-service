package com.valeerp.sales;

import java.math.BigDecimal;
import java.time.Instant;

public record CatalogPromotionUpdateRequest(
        String code,
        String name,
        String kind,
        BigDecimal discountValue,
        String currency,
        Instant startsAt,
        Instant endsAt
) {
}
