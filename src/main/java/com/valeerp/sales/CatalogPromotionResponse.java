package com.valeerp.sales;

import java.math.BigDecimal;
import java.time.Instant;

public record CatalogPromotionResponse(
        Long id,
        String code,
        String name,
        String kind,
        BigDecimal discountValue,
        String currency,
        Instant startsAt,
        Instant endsAt,
        Instant createdAt
) {

    public static CatalogPromotionResponse from(CatalogPromotion promotion) {
        return new CatalogPromotionResponse(
                promotion.getId(),
                promotion.getCode(),
                promotion.getName(),
                promotion.getKind().name(),
                promotion.getDiscountValue(),
                promotion.getCurrency(),
                promotion.getStartsAt(),
                promotion.getEndsAt(),
                promotion.getCreatedAt()
        );
    }
}
