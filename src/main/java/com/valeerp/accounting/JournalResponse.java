package com.valeerp.accounting;

import java.time.Instant;

public record JournalResponse(
        Long id,
        Long companyId,
        String code,
        String name,
        String kind,
        Long defaultAccountId,
        Instant createdAt
) {

    public static JournalResponse from(Journal journal) {
        return new JournalResponse(
                journal.getId(),
                journal.getCompanyId(),
                journal.getCode(),
                journal.getName(),
                journal.getKind().name(),
                journal.getDefaultAccountId(),
                journal.getCreatedAt()
        );
    }
}
