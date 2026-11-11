package com.valeerp.sales;

import com.nomos.store.service.model.TaxRate;

import java.math.BigDecimal;

public record TaxRateResponse(Long id, String name, String kind, BigDecimal rate) {

    public static TaxRateResponse from(TaxRate taxRate) {
        return new TaxRateResponse(
                taxRate.getId(),
                taxRate.getName(),
                taxRate.getKind().name(),
                taxRate.getRate()
        );
    }
}
