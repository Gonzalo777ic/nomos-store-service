package com.valeerp.accounting;

public record ChartAccountUpdateRequest(
        String code,
        String name,
        String accountClass,
        Boolean allowsPosting,
        Long parentId
) {
}
