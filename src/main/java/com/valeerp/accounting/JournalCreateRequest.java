package com.valeerp.accounting;

public record JournalCreateRequest(
        Long companyId,
        String code,
        String name,
        String kind,
        String journalType,
        Long defaultAccountId
) {
}
