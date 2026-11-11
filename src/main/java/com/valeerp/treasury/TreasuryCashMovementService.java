package com.valeerp.treasury;

import com.nomos.store.service.model.CashMovement;
import com.nomos.store.service.model.CashMovementType;
import com.nomos.store.service.model.PaymentMethodConfig;
import com.nomos.store.service.repository.CashMovementRepository;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Libro de movimientos de efectivo/banco. Anulación solo por compensación.
 */
@Service
public class TreasuryCashMovementService {

    private final CashMovementRepository repository;
    private final PaymentMethodService paymentMethodService;
    private final CashSessionRepository cashSessionRepository;
    private final TimeProvider timeProvider;

    public TreasuryCashMovementService(CashMovementRepository repository,
                                       PaymentMethodService paymentMethodService,
                                       CashSessionRepository cashSessionRepository,
                                       TimeProvider timeProvider) {
        this.repository = repository;
        this.paymentMethodService = paymentMethodService;
        this.cashSessionRepository = cashSessionRepository;
        this.timeProvider = timeProvider;
    }

    public CashMovement require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE, "Movimiento de caja no encontrado"));
    }

    public List<CashMovement> listByDate(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start;
        LocalDateTime end;
        if (startDate == null || endDate == null) {
            start = LocalDate.now().atStartOfDay();
            end = LocalDate.now().atTime(LocalTime.MAX);
        } else {
            start = startDate.atStartOfDay();
            end = endDate.atTime(LocalTime.MAX);
        }
        return repository.findByMovementDateBetween(start, end);
    }

    public List<CashMovement> listBySession(Long sessionId) {
        return repository.findByCashSessionIdAndStatus(
                sessionId, com.nomos.store.service.model.CashMovementStatus.PROCESSED);
    }

    public Money expectedCashForSession(CashSession session) {
        Money expected = session.openingCash();
        for (CashMovement movement : listBySession(session.getId())) {
            if (movement.getChannel() != CashChannel.CASH_REGISTER) {
                continue;
            }
            expected = expected.plus(movement.signedEffectOnCash());
        }
        return expected;
    }

    @Transactional
    public CashMovement createManual(CashMovementCreateRequest request) {
        CashMovementType type = CashMovementType.require(request.type());
        if (type == CashMovementType.COMPENSATION) {
            throw new DomainException("La compensación solo se crea al anular un movimiento");
        }
        CashChannel channel = CashChannel.require(request.channel());
        Money money = resolveMoney(request.amount(), request.currency());
        PaymentMethodConfig method = request.paymentMethodId() == null
                ? null
                : paymentMethodService.require(request.paymentMethodId());
        if (method != null && method.isRequiresReference()
                && (request.externalReference() == null || request.externalReference().isBlank())) {
            throw new DomainException("El método de pago exige referencia externa");
        }
        Long sessionId = request.cashSessionId();
        if (channel == CashChannel.CASH_REGISTER && sessionId != null) {
            CashSession session = cashSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new DomainException(
                            DomainExceptionHandler.NOT_FOUND_CODE, "Sesión de caja no encontrada"));
            if (!session.isOpen()) {
                throw new DomainException("La sesión de caja está cerrada");
            }
            if (request.cashRegisterId() != null
                    && !request.cashRegisterId().equals(session.getCashRegisterId())) {
                throw new DomainException("La sesión no pertenece a la caja indicada");
            }
        }
        CashMovement movement = CashMovement.record(
                type,
                money,
                channel,
                method,
                request.cashRegisterId(),
                sessionId,
                request.bankAccountId(),
                request.concept(),
                request.externalReference(),
                TreasuryPolicy.SOURCE_MANUAL,
                null,
                Actors.currentActor(),
                timeProvider.now()
        );
        return repository.save(movement);
    }

    @Transactional
    public CashMovement voidMovement(Long id, String reason) {
        CashMovement original = require(id);
        CashMovement compensation = original.voidWithCompensation(reason, Actors.currentActor(), timeProvider.now());
        repository.save(original);
        return repository.save(compensation);
    }

    @Transactional
    public CashMovement save(CashMovement movement) {
        return repository.save(movement);
    }

    private Money resolveMoney(java.math.BigDecimal amount, String currency) {
        String resolved = currency == null || currency.isBlank()
                ? TreasuryPolicy.DEFAULT_CURRENCY.code()
                : currency;
        return Money.of(amount, resolved);
    }
}
