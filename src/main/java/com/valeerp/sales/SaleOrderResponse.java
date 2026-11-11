package com.valeerp.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleOrderResponse(
        Long id,
        Long partnerId,
        Long opportunityId,
        String status,
        String invoicingPolicy,
        String currency,
        Instant validUntil,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal globalDiscountAmount,
        BigDecimal baseAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String globalDiscountKind,
        BigDecimal globalDiscountValue,
        Long sellerId,
        Long salesTeamId,
        Long territoryId,
        Long commissionRuleId,
        String notes,
        Instant createdAt,
        Instant confirmedAt,
        Instant cancelledAt,
        List<SaleOrderLineResponse> lines,
        SaleOrderDocumentResponse commercialDocument
) {

    public static SaleOrderResponse from(SaleOrder order) {
        return new SaleOrderResponse(
                order.getId(),
                order.getPartnerId(),
                order.getOpportunityId(),
                order.getStatus().name(),
                order.getInvoicingPolicy().name(),
                order.getCurrency(),
                order.getValidUntil(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getGlobalDiscountAmount(),
                order.getBaseAmount(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getGlobalDiscountKind() == null ? null : order.getGlobalDiscountKind().name(),
                order.getGlobalDiscountValue(),
                order.getSellerId(),
                order.getSalesTeamId(),
                order.getTerritoryId(),
                order.getCommissionRuleId(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getCancelledAt(),
                order.getLines().stream().map(SaleOrderLineResponse::from).toList(),
                SaleOrderDocumentResponse.from(order.getCommercialDocument())
        );
    }
}
