package com.valeerp.pos;

import java.time.Instant;
import java.util.List;

public record PosTicketResponse(
        Long id,
        Long posConfigId,
        Long companyId,
        Long cashSessionId,
        Long partnerId,
        String status,
        PosMoneyView subtotalBase,
        PosMoneyView taxTotal,
        PosMoneyView grandTotal,
        String currency,
        Instant openedAt,
        Instant postedAt,
        Instant voidedAt,
        String voidReason,
        List<PosTicketLineView> lines,
        List<PosTicketPaymentView> payments,
        List<PosReversalDocumentView> reversals
) {

    public static PosTicketResponse from(PosTicket ticket, List<PosReversalDocument> reversals) {
        return new PosTicketResponse(
                ticket.getId(),
                ticket.getPosConfigId(),
                ticket.getCompanyId(),
                ticket.getCashSessionId(),
                ticket.getPartnerId(),
                ticket.getStatus().name(),
                PosMoneyView.from(ticket.baseMoney()),
                PosMoneyView.from(ticket.taxMoney()),
                PosMoneyView.from(ticket.totalMoney()),
                ticket.getCurrency(),
                ticket.getOpenedAt(),
                ticket.getPostedAt(),
                ticket.getVoidedAt(),
                ticket.getVoidReason(),
                ticket.getLines().stream().map(PosTicketLineView::from).toList(),
                ticket.getPayments().stream().map(PosTicketPaymentView::from).toList(),
                reversals == null
                        ? List.of()
                        : reversals.stream().map(PosReversalDocumentView::from).toList()
        );
    }

    public static PosTicketResponse from(PosTicket ticket) {
        return from(ticket, List.of());
    }
}
