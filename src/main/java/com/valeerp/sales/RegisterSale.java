package com.valeerp.sales;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RegisterSale(
        Long clientId,
        LocalDateTime saleDate,
        LocalDate creditStartDate,
        String type,
        String paymentCondition,
        Integer creditDays,
        Long sellerId,
        Integer numberOfInstallments,
        List<Line> details
) {
    public record Line(Long productId, Integer quantity, Long taxRateId, Long promotionId) {
    }
}
