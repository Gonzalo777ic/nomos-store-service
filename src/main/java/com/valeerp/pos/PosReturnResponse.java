package com.valeerp.pos;

import java.time.Instant;
import java.util.List;

public record PosReturnResponse(
        Long id,
        Long ticketId,
        String status,
        String reason,
        PosMoneyView refundTotal,
        String currency,
        Instant createdAt,
        Instant confirmedAt,
        List<PosReturnLineView> lines
) {

    public static PosReturnResponse from(PosReturn posReturn) {
        return new PosReturnResponse(
                posReturn.getId(),
                posReturn.getTicketId(),
                posReturn.getStatus().name(),
                posReturn.getReason(),
                PosMoneyView.from(posReturn.refundMoney()),
                posReturn.getCurrency(),
                posReturn.getCreatedAt(),
                posReturn.getConfirmedAt(),
                posReturn.getLines().stream().map(PosReturnLineView::from).toList()
        );
    }
}
