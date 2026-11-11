package com.valeerp.sales;

import java.math.BigDecimal;

/**
 * Alta de línea. {@code clientSubtotal} se ignora a propósito: el agregado calcula totales.
 */
public record SaleOrderLineRequest(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal clientSubtotal,
        Long promotionId,
        String lineDiscountKind,
        BigDecimal lineDiscountValue,
        Long taxRateId
) {
}
