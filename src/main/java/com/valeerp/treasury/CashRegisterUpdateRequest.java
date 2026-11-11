package com.valeerp.treasury;

public record CashRegisterUpdateRequest(
        String code,
        String name,
        Long journalId,
        String currency
) {
}
