package com.valeerp.treasury;

import java.time.Instant;

public record CashSessionResponse(
        Long id,
        Long cashRegisterId,
        String status,
        Instant openedAt,
        String openedBy,
        Instant closedAt,
        String closedBy,
        MoneyDto openingCash,
        MoneyDto expectedCash,
        MoneyDto countedCash,
        MoneyDto difference,
        String differenceNote
) {

    public static CashSessionResponse from(CashSession session) {
        return new CashSessionResponse(
                session.getId(),
                session.getCashRegisterId(),
                session.getStatus().name(),
                session.getOpenedAt(),
                session.getOpenedBy(),
                session.getClosedAt(),
                session.getClosedBy(),
                MoneyDto.from(session.openingCash()),
                MoneyDto.from(session.expectedCash()),
                MoneyDto.from(session.countedCash()),
                MoneyDto.from(session.difference()),
                session.getDifferenceNote()
        );
    }
}
