package com.nomos.store.service.model.dto;

import com.nomos.store.service.model.CashMovement;
import com.nomos.store.service.model.CashMovementStatus;
import com.nomos.store.service.model.CashMovementType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CashMovementDTO {
    private Long id;
    private LocalDateTime movementDate;
    private CashMovementType type;
    private BigDecimal amount;
    private String currency;
    private String paymentMethodName;
    private String concept;
    private String externalReference;
    private CashMovementStatus status;
    private Long saleId;
    private Long compensatesMovementId;

    public static CashMovementDTO fromEntity(CashMovement entity) {
        return CashMovementDTO.builder()
                .id(entity.getId())
                .movementDate(entity.getMovementDate())
                .type(entity.getType())
                .amount(entity.getAmountValue())
                .currency(entity.getCurrencyCode())
                .paymentMethodName(entity.getPaymentMethod() != null ? entity.getPaymentMethod().getName() : null)
                .concept(entity.getConcept())
                .externalReference(entity.getExternalReference())
                .status(entity.getStatus())
                .saleId(entity.getSale() != null ? entity.getSale().getId() : null)
                .compensatesMovementId(entity.getCompensatesMovementId())
                .build();
    }
}
