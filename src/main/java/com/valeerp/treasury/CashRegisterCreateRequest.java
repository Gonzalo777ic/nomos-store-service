package com.valeerp.treasury;

public record CashRegisterCreateRequest(
        Long companyId,
        String code,
        String name,
        Long journalId,
        String currency
) {
}
