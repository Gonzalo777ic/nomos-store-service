package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceDocumentResponse(
        Long id,
        Long companyId,
        Long partnerId,
        String side,
        String documentNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        Long taxRateId,
        BigDecimal untaxedAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal residual,
        String currencyCode,
        Long proposedJournalEntryId,
        String status
) {

    public static InvoiceDocumentResponse from(InvoiceDocument invoice) {
        return new InvoiceDocumentResponse(
                invoice.getId(),
                invoice.getCompanyId(),
                invoice.getPartnerId(),
                invoice.getSide().name(),
                invoice.getDocumentNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getTaxRateId(),
                invoice.untaxed().amount(),
                invoice.tax().amount(),
                invoice.total().amount(),
                invoice.paidAmount().amount(),
                invoice.residual().amount(),
                invoice.getCurrencyCode(),
                invoice.getProposedJournalEntryId(),
                invoice.getStatus().name()
        );
    }
}
