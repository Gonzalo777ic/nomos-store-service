package com.valeerp.treasury;

import com.nomos.store.service.model.CashMovement;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashSessionService {

    private final CashSessionRepository cashSessionRepository;
    private final CashRegisterService cashRegisterService;
    private final TreasuryCashMovementService cashMovementService;
    private final TimeProvider timeProvider;

    public CashSessionService(CashSessionRepository cashSessionRepository,
                              CashRegisterService cashRegisterService,
                              TreasuryCashMovementService cashMovementService,
                              TimeProvider timeProvider) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashRegisterService = cashRegisterService;
        this.cashMovementService = cashMovementService;
        this.timeProvider = timeProvider;
    }

    public Page<CashSession> listByRegister(Long cashRegisterId, Pageable pageable) {
        cashRegisterService.require(cashRegisterId);
        return cashSessionRepository.findByCashRegisterId(cashRegisterId, pageable);
    }

    public CashSession require(Long id) {
        return cashSessionRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE, "Sesión de caja no encontrada"));
    }

    public CashSession requireOpenForRegister(Long cashRegisterId) {
        return cashSessionRepository.findByCashRegisterIdAndStatus(cashRegisterId, CashSessionStatus.OPEN)
                .orElseThrow(() -> new DomainException("La caja no tiene sesión abierta"));
    }

    @Transactional
    public CashSession open(CashSessionOpenRequest request) {
        CashRegister register = cashRegisterService.require(request.cashRegisterId());
        if (cashSessionRepository.existsByCashRegisterIdAndStatus(register.getId(), CashSessionStatus.OPEN)) {
            throw new DomainException("La caja ya tiene una sesión abierta");
        }
        String currency = request.currency() == null || request.currency().isBlank()
                ? register.getCurrencyCode()
                : request.currency();
        if (!register.getCurrencyCode().equalsIgnoreCase(currency)) {
            throw new DomainException("La moneda de apertura no coincide con la de la caja");
        }
        Money opening = Money.of(request.openingAmount(), currency);
        CashSession session = CashSession.open(
                register.getId(),
                opening,
                Actors.currentActor(),
                timeProvider.now()
        );
        return cashSessionRepository.save(session);
    }

    @Transactional
    public CashSession close(Long sessionId, CashSessionCloseRequest request) {
        CashSession session = require(sessionId);
        if (!session.isOpen()) {
            throw new DomainException("La sesión ya está cerrada");
        }
        Money expected = cashMovementService.expectedCashForSession(session);
        String currency = request.currency() == null || request.currency().isBlank()
                ? session.getCurrencyCode()
                : request.currency();
        Money counted = Money.of(request.countedAmount(), currency);
        session.close(expected, counted, request.differenceNote(), Actors.currentActor(), timeProvider.now());
        CashSession saved = cashSessionRepository.save(session);

        if (saved.hasDifference()) {
            CashMovement adjustment = CashMovement.arqueoAdjustment(
                    saved.difference(),
                    saved.getCashRegisterId(),
                    saved.getId(),
                    saved.getDifferenceNote(),
                    Actors.currentActor(),
                    timeProvider.now()
            );
            cashMovementService.save(adjustment);
        }
        return saved;
    }
}
