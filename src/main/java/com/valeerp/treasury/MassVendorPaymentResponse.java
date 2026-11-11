package com.valeerp.treasury;

import java.time.Instant;
import java.util.List;

public record MassVendorPaymentResponse(
        Long id,
        Long companyId,
        String reference,
        String status,
        Long paymentMethodId,
        Long bankAccountId,
        Long cashRegisterId,
        Long cashSessionId,
        MoneyDto total,
        String concept,
        Instant createdAt,
        Instant confirmedAt,
        Instant postedAt,
        Instant cancelledAt,
        String createdBy,
        List<MassVendorPaymentLineResponse> lines
) {

    public static MassVendorPaymentResponse from(MassVendorPayment batch) {
        return new MassVendorPaymentResponse(
                batch.getId(),
                batch.getCompanyId(),
                batch.getReference(),
                batch.getStatus().name(),
                batch.getPaymentMethodId(),
                batch.getBankAccountId(),
                batch.getCashRegisterId(),
                batch.getCashSessionId(),
                MoneyDto.from(batch.total()),
                batch.getConcept(),
                batch.getCreatedAt(),
                batch.getConfirmedAt(),
                batch.getPostedAt(),
                batch.getCancelledAt(),
                batch.getCreatedBy(),
                batch.getLines().stream().map(MassVendorPaymentLineResponse::from).toList()
        );
    }
}
