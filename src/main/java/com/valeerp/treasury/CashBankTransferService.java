package com.valeerp.treasury;

import com.nomos.store.service.model.CashMovement;
import com.nomos.store.service.model.CashMovementType;
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
public class CashBankTransferService {

    private final CashBankTransferRepository transferRepository;
    private final CashRegisterService cashRegisterService;
    private final CashSessionRepository cashSessionRepository;
    private final TreasuryCashMovementService cashMovementService;
    private final TimeProvider timeProvider;

    public CashBankTransferService(CashBankTransferRepository transferRepository,
                                   CashRegisterService cashRegisterService,
                                   CashSessionRepository cashSessionRepository,
                                   TreasuryCashMovementService cashMovementService,
                                   TimeProvider timeProvider) {
        this.transferRepository = transferRepository;
        this.cashRegisterService = cashRegisterService;
        this.cashSessionRepository = cashSessionRepository;
        this.cashMovementService = cashMovementService;
        this.timeProvider = timeProvider;
    }

    public Page<CashBankTransfer> list(Long companyId, Pageable pageable) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        return transferRepository.findByCompanyId(companyId, pageable);
    }

    public CashBankTransfer require(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE, "Transferencia caja-banco no encontrada"));
    }

    @Transactional
    public CashBankTransfer create(CashBankTransferCreateRequest request) {
        CashRegister register = cashRegisterService.require(request.cashRegisterId());
        TransferDirection direction = TransferDirection.require(request.direction());
        String currency = request.currency() == null || request.currency().isBlank()
                ? register.getCurrencyCode()
                : request.currency();
        if (!register.getCurrencyCode().equalsIgnoreCase(currency)) {
            throw new DomainException("La moneda no coincide con la de la caja");
        }
        Long sessionId = request.cashSessionId();
        if (sessionId != null) {
            CashSession session = cashSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new DomainException(
                            DomainExceptionHandler.NOT_FOUND_CODE, "Sesión de caja no encontrada"));
            if (!session.getCashRegisterId().equals(register.getId())) {
                throw new DomainException("La sesión no pertenece a la caja");
            }
            if (!session.isOpen()) {
                throw new DomainException("La sesión de caja está cerrada");
            }
        }
        Money money = Money.of(request.amount(), currency);
        CashBankTransfer transfer = CashBankTransfer.draft(
                request.companyId(),
                direction,
                register.getId(),
                sessionId,
                request.bankAccountId(),
                money,
                request.concept(),
                Actors.currentActor(),
                timeProvider.now()
        );
        return transferRepository.save(transfer);
    }

    @Transactional
    public CashBankTransfer post(Long id) {
        CashBankTransfer transfer = require(id);
        Money money = transfer.money();
        boolean cashToBank = transfer.getDirection() == TransferDirection.CASH_TO_BANK;

        CashMovement cashSide = CashMovement.record(
                cashToBank ? CashMovementType.TRANSFER_OUT : CashMovementType.TRANSFER_IN,
                money,
                CashChannel.CASH_REGISTER,
                null,
                transfer.getCashRegisterId(),
                transfer.getCashSessionId(),
                null,
                transfer.getConcept(),
                null,
                TreasuryPolicy.SOURCE_CASH_BANK_TRANSFER,
                transfer.getId(),
                Actors.currentActor(),
                timeProvider.now()
        );
        CashMovement bankSide = CashMovement.record(
                cashToBank ? CashMovementType.TRANSFER_IN : CashMovementType.TRANSFER_OUT,
                money,
                CashChannel.BANK,
                null,
                null,
                null,
                transfer.getBankAccountId(),
                transfer.getConcept(),
                null,
                TreasuryPolicy.SOURCE_CASH_BANK_TRANSFER,
                transfer.getId(),
                Actors.currentActor(),
                timeProvider.now()
        );
        cashSide = cashMovementService.save(cashSide);
        bankSide = cashMovementService.save(bankSide);
        transfer.markPosted(cashSide.getId(), bankSide.getId(), timeProvider.now());
        return transferRepository.save(transfer);
    }

    @Transactional
    public CashBankTransfer voidTransfer(Long id, String reason) {
        CashBankTransfer transfer = require(id);
        if (transfer.getCashMovementId() != null) {
            cashMovementService.voidMovement(transfer.getCashMovementId(), reason);
        }
        if (transfer.getBankMovementId() != null) {
            cashMovementService.voidMovement(transfer.getBankMovementId(), reason);
        }
        transfer.markVoided(timeProvider.now());
        return transferRepository.save(transfer);
    }
}
