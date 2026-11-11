package com.valeerp.payable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AccountsPayableResponse(
        Long id,
        Long companyId,
        Long partnerId,
        Long supplierInvoiceId,
        String documentNumber,
        LocalDate issueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        String currencyCode,
        String status,
        Instant voidedAt,
        Long reversalJournalEntryId,
        List<InstallmentResponse> installments
) {

    public record InstallmentResponse(
            Long id,
            Integer number,
            LocalDate dueDate,
            BigDecimal expectedAmount,
            BigDecimal paidAmount,
            String status
    ) {
    }

    public static AccountsPayableResponse from(AccountsPayable payable) {
        return new AccountsPayableResponse(
                payable.getId(),
                payable.getCompanyId(),
                payable.getPartnerId(),
                payable.getSupplierInvoiceId(),
                payable.getDocumentNumber(),
                payable.getIssueDate(),
                payable.total().amount(),
                payable.paidAmount().amount(),
                payable.balance().amount(),
                payable.getCurrencyCode(),
                payable.getStatus().name(),
                payable.getVoidedAt(),
                payable.getReversalJournalEntryId(),
                payable.getInstallments().stream()
                        .map(i -> new InstallmentResponse(
                                i.getId(),
                                i.getNumber(),
                                i.getDueDate(),
                                i.expected().amount(),
                                i.paid().amount(),
                                i.getStatus().name()
                        ))
                        .toList()
        );
    }
}
