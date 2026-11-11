package com.valeerp.pos;

public record PosConfigCreateRequest(
        Long companyId,
        String code,
        String name,
        Long cashRegisterId,
        Long journalId,
        Long priceListId,
        String currency
) {
}
