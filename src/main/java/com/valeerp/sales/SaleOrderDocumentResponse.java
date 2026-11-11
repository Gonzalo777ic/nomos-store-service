package com.valeerp.sales;

import java.math.BigDecimal;
import java.time.Instant;

public record SaleOrderDocumentResponse(
        Long id,
        Long saleOrderId,
        String series,
        String number,
        String fullNumber,
        Instant issuedAt,
        BigDecimal totalAmount,
        String currency,
        String invoicingPolicy
) {

    public static SaleOrderDocumentResponse from(SaleOrderDocument document) {
        if (document == null) {
            return null;
        }
        return new SaleOrderDocumentResponse(
                document.getId(),
                document.getSaleOrderId(),
                document.getSeries(),
                document.getNumber(),
                document.fullNumber(),
                document.getIssuedAt(),
                document.getTotalAmount(),
                document.getCurrency(),
                document.getInvoicingPolicy()
        );
    }
}
