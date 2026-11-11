package com.valeerp.pos;

import java.math.BigDecimal;

public record PosTicketLineView(
        Long id,
        Long productId,
        String productName,
        int quantity,
        int quantityReturned,
        PosMoneyView unitPrice,
        Long taxRateId,
        BigDecimal taxRate,
        PosMoneyView lineBase,
        PosMoneyView lineTax,
        PosMoneyView lineNet
) {

    public static PosTicketLineView from(PosTicketLine line) {
        return new PosTicketLineView(
                line.getId(),
                line.getProductId(),
                line.getProductName(),
                line.getQuantity(),
                line.getQuantityReturned(),
                PosMoneyView.from(line.unitPriceMoney()),
                line.getTaxRateId(),
                line.getTaxRate(),
                PosMoneyView.from(line.baseMoney()),
                PosMoneyView.from(line.taxMoney()),
                PosMoneyView.from(line.netMoney())
        );
    }
}
