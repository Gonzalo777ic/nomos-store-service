package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceDocumentCreateRequest(
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
        String currencyCode
) {
}
