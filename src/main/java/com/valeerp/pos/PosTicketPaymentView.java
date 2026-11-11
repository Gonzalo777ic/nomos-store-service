package com.valeerp.pos;

public record PosTicketPaymentView(
        Long id,
        Long paymentMethodId,
        PosMoneyView amount
) {

    public static PosTicketPaymentView from(PosTicketPayment payment) {
        return new PosTicketPaymentView(
                payment.getId(),
                payment.getPaymentMethodId(),
                PosMoneyView.from(payment.money())
        );
    }
}
