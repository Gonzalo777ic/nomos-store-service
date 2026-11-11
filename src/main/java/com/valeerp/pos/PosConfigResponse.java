package com.valeerp.pos;

import java.time.Instant;

public record PosConfigResponse(
        Long id,
        Long companyId,
        String code,
        String name,
        Long cashRegisterId,
        Long journalId,
        Long priceListId,
        String currency,
        Instant createdAt
) {

    public static PosConfigResponse from(PosConfig config) {
        return new PosConfigResponse(
                config.getId(),
                config.getCompanyId(),
                config.getCode(),
                config.getName(),
                config.getCashRegisterId(),
                config.getJournalId(),
                config.getPriceListId(),
                config.getCurrency(),
                config.getCreatedAt()
        );
    }
}
