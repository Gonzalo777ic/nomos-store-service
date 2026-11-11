package com.valeerp.accounting;

import java.time.Instant;

public record ChartAccountResponse(
        Long id,
        Long companyId,
        String code,
        String name,
        String accountClass,
        boolean allowsPosting,
        Long parentId,
        Instant createdAt
) {

    public static ChartAccountResponse from(ChartAccount account) {
        return new ChartAccountResponse(
                account.getId(),
                account.getCompanyId(),
                account.getCode(),
                account.getName(),
                account.getAccountClass().name(),
                account.isAllowsPosting(),
                account.getParentId(),
                account.getCreatedAt()
        );
    }
}
