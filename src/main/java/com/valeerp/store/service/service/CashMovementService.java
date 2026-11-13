package com.valeerp.store.service.service;

import com.valeerp.store.service.model.CashMovement;
import com.valeerp.store.service.model.CashMovementType;
import com.valeerp.store.service.model.Collection;
import com.valeerp.store.service.model.PaymentMethodConfig;
import com.valeerp.kernel.Actor;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.treasury.CashChannel;
import com.valeerp.treasury.CashMovementCreateRequest;
import com.valeerp.treasury.TreasuryCashMovementService;
import com.valeerp.treasury.TreasuryPolicy;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador legacy para cobranza y rutas {@code /api/store/cash-movements}.
 * El libro vive en {@link TreasuryCashMovementService}.
 */
@Service
public class CashMovementService {

    private final TreasuryCashMovementService treasuryCashMovementService;
    private final TimeProvider timeProvider;

    public CashMovementService(TreasuryCashMovementService treasuryCashMovementService,
                               TimeProvider timeProvider) {
        this.treasuryCashMovementService = treasuryCashMovementService;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public CashMovement registerIncomeFromCollection(Collection collection, Long userId) {
        CashMovement movement = CashMovement.incomeFromCollection(
                collection, Actor.of(userId == null ? null : String.valueOf(userId)), timeProvider.now());
        movement.attachCreatedByUserId(userId);
        return treasuryCashMovementService.save(movement);
    }

    @Transactional
    public CashMovement registerExpense(Double amount, PaymentMethodConfig method, String reason, Long userId) {
        Money money = Money.of(BigDecimal.valueOf(amount), TreasuryPolicy.DEFAULT_CURRENCY.code());
        CashMovement movement = CashMovement.record(
                CashMovementType.EXPENSE,
                money,
                CashChannel.CASH_REGISTER,
                method,
                null,
                null,
                null,
                reason,
                null,
                TreasuryPolicy.SOURCE_MANUAL,
                null,
                Actor.of(userId == null ? null : String.valueOf(userId)),
                timeProvider.now()
        );
        movement.attachCreatedByUserId(userId);
        return treasuryCashMovementService.save(movement);
    }

    public List<CashMovement> getDailyMovements() {
        LocalDate today = LocalDate.now();
        return treasuryCashMovementService.listByDate(today, today);
    }

    @Transactional
    public CashMovement createManualMovement(ManualMovementPayload payload, Long userId) {
        CashMovementCreateRequest request = new CashMovementCreateRequest(
                payload.getType() == null ? null : payload.getType().name(),
                payload.getAmount() == null ? null : BigDecimal.valueOf(payload.getAmount()),
                TreasuryPolicy.DEFAULT_CURRENCY.code(),
                CashChannel.CASH_REGISTER.name(),
                payload.getPaymentMethodId(),
                null,
                null,
                null,
                payload.getConcept(),
                payload.getExternalReference()
        );
        CashMovement movement = treasuryCashMovementService.createManual(request);
        movement.attachCreatedByUserId(userId);
        return treasuryCashMovementService.save(movement);
    }

    public List<CashMovement> getMovementsByFilter(LocalDate startDate, LocalDate endDate) {
        return treasuryCashMovementService.listByDate(startDate, endDate);
    }

    @Data
    public static class ManualMovementPayload {
        private CashMovementType type;
        private Double amount;
        private Long paymentMethodId;
        private String concept;
        private String externalReference;
    }
}
