package com.valeerp.pos;

public record PosConfigUpdateRequest(
        String name,
        Long cashRegisterId,
        Long journalId,
        Long priceListId,
        String currency
) {
}
