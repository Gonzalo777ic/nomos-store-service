package com.valeerp.treasury;

import java.time.Instant;

public record CashBankTransferResponse(
        Long id,
        Long companyId,
        String direction,
        String status,
        Long cashRegisterId,
        Long cashSessionId,
        Long bankAccountId,
        MoneyDto amount,
        String concept,
        Long cashMovementId,
        Long bankMovementId,
        Instant createdAt,
        Instant postedAt,
        Instant voidedAt,
        String createdBy
) {

    public static CashBankTransferResponse from(CashBankTransfer transfer) {
        return new CashBankTransferResponse(
                transfer.getId(),
                transfer.getCompanyId(),
                transfer.getDirection().name(),
                transfer.getStatus().name(),
                transfer.getCashRegisterId(),
                transfer.getCashSessionId(),
                transfer.getBankAccountId(),
                MoneyDto.from(transfer.money()),
                transfer.getConcept(),
                transfer.getCashMovementId(),
                transfer.getBankMovementId(),
                transfer.getCreatedAt(),
                transfer.getPostedAt(),
                transfer.getVoidedAt(),
                transfer.getCreatedBy()
        );
    }
}
