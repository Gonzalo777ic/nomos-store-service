package com.nomos.store.service.service;

import com.nomos.store.service.model.*;
import com.nomos.store.service.repository.CashMovementRepository;
import com.nomos.store.service.repository.PaymentMethodConfigRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashMovementService {

    private final CashMovementRepository repository;
    private final PaymentMethodConfigRepository paymentMethodRepository;

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



    /**
     * Obtener movimientos filtrados por rango de fechas.
     * Si las fechas son nulas, devuelve los del día actual.
     */
    public List<CashMovement> getMovementsByFilter(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start;
        LocalDateTime end;

        if (startDate == null || endDate == null) {
            // Por defecto: HOY
            start = LocalDate.now().atStartOfDay();
            end = LocalDate.now().atTime(LocalTime.MAX);
        } else {
            start = startDate.atStartOfDay();
            end = endDate.atTime(LocalTime.MAX);
        }

        // Usamos el método que ya tenías en el repositorio
        return repository.findByMovementDateBetween(start, end);
    }

    // DTO Interno para el payload manual
    @Data
    public static class ManualMovementPayload {
        private CashMovementType type;
        private Double amount;
        private Long paymentMethodId;
        private String concept;
        private String externalReference;
    }


}