package com.valeerp.accounting;

import java.math.BigDecimal;
import java.util.List;

public record PaymentReconcileRequest(List<AllocationLine> allocations) {

    public record AllocationLine(Long invoiceId, BigDecimal amount) {
    }
}
