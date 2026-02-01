package com.nomos.store.service.service;

import com.nomos.store.service.model.*;
import com.nomos.store.service.repository.CashMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashMovementService {

    private final CashMovementRepository repository;

    /**
     * Registra un ingreso de dinero derivado de una cobranza (Collection).
     * Este método debe ser llamado DESPUÉS de guardar la Collection.
     */
    @Transactional
    public CashMovement registerIncomeFromCollection(Collection collection, Long userId) {

        CashMovementStatus status = CashMovementStatus.PROCESSED;

        CashMovement movement = CashMovement.builder()
                .type(CashMovementType.INCOME)
                .amount(collection.getAmount())
                .movementDate(LocalDateTime.now())
                .paymentMethod(collection.getPaymentMethod())
                .collection(collection)
                .sale(collection.getSale())
                .externalReference(collection.getReferenceNumber())
                .concept("Cobro Venta " + collection.getSale().getId())
                .status(status)
                .createdByUserId(userId)
                .build();

        return repository.save(movement);
    }

    /**
     * Registra un egreso (ej: Devolución de dinero por nota de crédito)
     */
    @Transactional
    public CashMovement registerExpense(Double amount, PaymentMethodConfig method, String reason, Long userId) {
        CashMovement movement = CashMovement.builder()
                .type(CashMovementType.EXPENSE)
                .amount(amount)
                .movementDate(LocalDateTime.now())
                .paymentMethod(method)
                .concept(reason)
                .status(CashMovementStatus.PROCESSED)
                .createdByUserId(userId)
                .build();

        return repository.save(movement);
    }

    public List<CashMovement> getDailyMovements() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return repository.findByMovementDateBetween(startOfDay, endOfDay);
    }


}