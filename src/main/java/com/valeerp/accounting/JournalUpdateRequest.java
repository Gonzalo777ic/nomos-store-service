package com.valeerp.accounting;

public record JournalUpdateRequest(
        String code,
        String name,
        String kind,
        String journalType,
        Long defaultAccountId
) {
}
