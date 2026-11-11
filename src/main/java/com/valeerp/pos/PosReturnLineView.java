package com.valeerp.pos;

public record PosReturnLineView(
        Long id,
        Long ticketLineId,
        int quantity,
        PosMoneyView refundAmount
) {

    public static PosReturnLineView from(PosReturnLine line) {
        return new PosReturnLineView(
                line.getId(),
                line.getTicketLineId(),
                line.getQuantity(),
                PosMoneyView.from(line.refundMoney())
        );
    }
}
