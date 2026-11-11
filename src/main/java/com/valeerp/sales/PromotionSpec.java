package com.valeerp.sales;

import java.math.BigDecimal;

/**
 * Instantánea de promoción aplicada dentro del agregado SaleOrder (sin JPA en el contrato de dominio).
 */
public record PromotionSpec(
        Long promotionId,
        PromotionKind kind,
        BigDecimal discountValue,
        String currency
) {
}
