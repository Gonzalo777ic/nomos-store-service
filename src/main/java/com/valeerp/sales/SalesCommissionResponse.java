package com.valeerp.sales;

import java.math.BigDecimal;
import java.time.Instant;

public record SalesCommissionResponse(
        Long id,
        Long saleId,
        Long saleOrderId,
        Long sellerId,
        Long commissionRuleId,
        String ruleCode,
        BigDecimal percentRate,
        BigDecimal baseAmount,
        BigDecimal commissionAmount,
        String currency,
        String status,
        Instant accruedAt,
        Instant cancelledAt
) {

    public static SalesCommissionResponse from(SalesCommission commission) {
        return new SalesCommissionResponse(
                commission.getId(),
                commission.getSaleId(),
                commission.getSaleOrderId(),
                commission.getSellerId(),
                commission.getCommissionRuleId(),
                commission.getRuleCode(),
                commission.getPercentRate(),
                commission.getBaseAmount(),
                commission.getCommissionAmount(),
                commission.getCurrency(),
                commission.getStatus().name(),
                commission.getAccruedAt(),
                commission.getCancelledAt()
        );
    }
}
