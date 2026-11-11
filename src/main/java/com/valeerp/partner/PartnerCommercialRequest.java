package com.valeerp.partner;

import java.math.BigDecimal;

/**
 * Asignación de categoría, lista de precios, condición de pago y límite de crédito.
 */
public record PartnerCommercialRequest(
        Long categoryId,
        Long priceListId,
        Long paymentTermId,
        BigDecimal creditLimitAmount,
        String creditLimitCurrency,
        Boolean clearCreditLimit
) {
}
