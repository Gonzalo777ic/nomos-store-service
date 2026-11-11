package com.valeerp.treasury;

import com.nomos.store.service.model.CashMovement;

import java.time.Instant;
import java.time.LocalDateTime;

public record CashMovementResponse(
        Long id,
        LocalDateTime movementDate,
        Instant occurredAt,
        String type,
        MoneyDto amount,
        String channel,
        Long cashRegisterId,
        Long cashSessionId,
        Long bankAccountId,
        Long paymentMethodId,
        String paymentMethodName,
        String concept,
        String externalReference,
        String status,
        String sourceDocumentType,
        Long sourceDocumentId,
        Long compensatesMovementId,
        Long saleId
) {

    public static CashMovementResponse from(CashMovement movement) {
        return new CashMovementResponse(
                movement.getId(),
                movement.getMovementDate(),
                movement.getOccurredAt(),
                movement.getType().name(),
                MoneyDto.from(movement.money()),
                movement.getChannel().name(),
                movement.getCashRegisterId(),
                movement.getCashSessionId(),
                movement.getBankAccountId(),
                movement.getPaymentMethod() == null ? null : movement.getPaymentMethod().getId(),
                movement.getPaymentMethod() == null ? null : movement.getPaymentMethod().getName(),
                movement.getConcept(),
                movement.getExternalReference(),
                movement.getStatus().name(),
                movement.getSourceDocumentType(),
                movement.getSourceDocumentId(),
                movement.getCompensatesMovementId(),
                movement.getSale() == null ? null : movement.getSale().getId()
        );
    }
}
