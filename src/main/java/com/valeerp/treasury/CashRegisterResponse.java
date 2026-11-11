package com.valeerp.treasury;

import java.time.Instant;

public record CashRegisterResponse(
        Long id,
        Long companyId,
        String code,
        String name,
        Long journalId,
        String currency,
        Instant createdAt
) {

    public static CashRegisterResponse from(CashRegister register) {
        return new CashRegisterResponse(
                register.getId(),
                register.getCompanyId(),
                register.getCode(),
                register.getName(),
                register.getJournalId(),
                register.getCurrencyCode(),
                register.getCreatedAt()
        );
    }
}
