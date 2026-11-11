package com.valeerp.accounting;

public record ChartAccountCreateRequest(
        Long companyId,
        String code,
        String name,
        String accountClass,
        Boolean allowsPosting,
        Long parentId
) {
}
