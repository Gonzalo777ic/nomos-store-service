package com.valeerp.sales;

import java.math.BigDecimal;

public record SaleOrderLineResponse(
        Long id,
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineGross,
        BigDecimal lineDiscount,
        BigDecimal globalDiscountShare,
        BigDecimal baseAmount,
        BigDecimal taxAmount,
        BigDecimal lineTotal,
        Long taxRateId,
        BigDecimal taxRate,
        Long promotionId,
        String promotionKind,
        String explicitDiscountKind,
        BigDecimal explicitDiscountValue
) {

    public static SaleOrderLineResponse from(SaleOrderLine line) {
        return new SaleOrderLineResponse(
                line.getId(),
                line.getProductId(),
                line.getProductName(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineGross(),
                line.getLineDiscount(),
                line.getGlobalDiscountShare(),
                line.getBaseAmount(),
                line.getTaxAmount(),
                line.getLineTotal(),
                line.getTaxRateId(),
                line.getTaxRate(),
                line.getPromotionId(),
                line.getPromotionKind() == null ? null : line.getPromotionKind().name(),
                line.getExplicitDiscountKind() == null ? null : line.getExplicitDiscountKind().name(),
                line.getExplicitDiscountValue()
        );
    }
}
