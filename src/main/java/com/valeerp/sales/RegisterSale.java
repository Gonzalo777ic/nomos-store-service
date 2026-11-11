package com.valeerp.sales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Comando de alta de venta. Los subtotales del cliente no forman parte del contrato.
 */
public record RegisterSale(
        Long clientId,
        LocalDateTime saleDate,
        LocalDate creditStartDate,
        String type,
        String paymentCondition,
        Integer creditDays,
        Long sellerId,
        Integer numberOfInstallments,
        String globalDiscountKind,
        BigDecimal globalDiscountValue,
        Long salesTeamId,
        Long territoryId,
        Long commissionRuleId,
        List<Line> details
) {
    public record Line(
            Long productId,
            Integer quantity,
            Long taxRateId,
            Long promotionId,
            String lineDiscountKind,
            BigDecimal lineDiscountValue
    ) {
        /** Compatibilidad con llamadas que solo envían producto/cantidad/tasa/promo. */
        public Line(Long productId, Integer quantity, Long taxRateId, Long promotionId) {
            this(productId, quantity, taxRateId, promotionId, null, null);
        }
    }
}
