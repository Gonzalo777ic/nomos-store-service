package com.valeerp.pos;

import java.time.Instant;

public record PosReversalDocumentView(
        Long id,
        Long ticketId,
        Long returnId,
        String kind,
        PosMoneyView amount,
        String reason,
        Instant createdAt
) {

    public static PosReversalDocumentView from(PosReversalDocument document) {
        return new PosReversalDocumentView(
                document.getId(),
                document.getTicketId(),
                document.getReturnId(),
                document.getKind().name(),
                PosMoneyView.from(document.money()),
                document.getReason(),
                document.getCreatedAt()
        );
    }
}
