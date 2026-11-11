package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PaymentResponse(
        Long id,
        Long companyId,
        Long partnerId,
        String direction,
        LocalDate paymentDate,
        BigDecimal amount,
        BigDecimal allocatedAmount,
        BigDecimal unallocated,
        String currencyCode,
        String referenceNumber,
        String status,
        Instant confirmedAt,
        List<AllocationResponse> allocations
) {

    public record AllocationResponse(Long id, Long invoiceId, BigDecimal amount) {
    }

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCompanyId(),
                payment.getPartnerId(),
                payment.getDirection().name(),
                payment.getPaymentDate(),
                payment.amount().amount(),
                payment.allocatedAmount().amount(),
                payment.unallocated().amount(),
                payment.getCurrencyCode(),
                payment.getReferenceNumber(),
                payment.getStatus().name(),
                payment.getConfirmedAt(),
                payment.getAllocations().stream()
                        .map(a -> new AllocationResponse(
                                a.getId(),
                                a.getInvoice().getId(),
                                a.amount().amount()))
                        .toList()
        );
    }
}
