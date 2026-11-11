package com.valeerp.sales;

import com.nomos.store.service.model.SaleReturn;
import com.nomos.store.service.model.SaleReturnDetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleReturnResponse(
        Long id,
        Long saleId,
        LocalDateTime returnDate,
        String type,
        String status,
        String reason,
        BigDecimal totalRefundAmount,
        String currency,
        Long creditNoteId,
        List<Line> details
) {

    public static SaleReturnResponse from(SaleReturn saleReturn) {
        return new SaleReturnResponse(
                saleReturn.getId(),
                saleReturn.getSale() == null ? null : saleReturn.getSale().getId(),
                saleReturn.getReturnDate(),
                saleReturn.getType().name(),
                saleReturn.getStatus().name(),
                saleReturn.getReason(),
                saleReturn.getTotalRefundAmount(),
                saleReturn.getCurrency(),
                saleReturn.getCreditNote() == null ? null : saleReturn.getCreditNote().getId(),
                saleReturn.getDetails().stream().map(Line::from).toList()
        );
    }

    public record Line(Long id, Long originalSaleDetailId, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        static Line from(SaleReturnDetail detail) {
            return new Line(
                    detail.getId(),
                    detail.getOriginalSaleDetail() == null ? null : detail.getOriginalSaleDetail().getId(),
                    detail.getQuantity(),
                    detail.getUnitPrice(),
                    detail.getSubtotal()
            );
        }
    }
}
