package com.valeerp.treasury;

import com.nomos.store.service.model.CashMovement;
import com.nomos.store.service.model.CashMovementType;
import com.nomos.store.service.model.PaymentMethodConfig;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MassVendorPaymentService {

    private final MassVendorPaymentRepository repository;
    private final PaymentMethodService paymentMethodService;
    private final TreasuryCashMovementService cashMovementService;
    private final TimeProvider timeProvider;

    public MassVendorPaymentService(MassVendorPaymentRepository repository,
                                    PaymentMethodService paymentMethodService,
                                    TreasuryCashMovementService cashMovementService,
                                    TimeProvider timeProvider) {
        this.repository = repository;
        this.paymentMethodService = paymentMethodService;
        this.cashMovementService = cashMovementService;
        this.timeProvider = timeProvider;
    }

    @Transactional(readOnly = true)
    public Page<MassVendorPayment> list(Long companyId, Pageable pageable) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        return repository.findByCompanyId(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public MassVendorPayment require(Long id) {
        MassVendorPayment batch = repository.findById(id)
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE, "Pago masivo no encontrado"));
        batch.getLines().size();
        return batch;
    }

    @Transactional
    public MassVendorPayment create(MassVendorPaymentCreateRequest request) {
        paymentMethodService.require(request.paymentMethodId());
        String currency = request.currency() == null || request.currency().isBlank()
                ? TreasuryPolicy.DEFAULT_CURRENCY.code()
                : request.currency();
        MassVendorPayment batch = MassVendorPayment.draft(
                request.companyId(),
                request.reference(),
                request.paymentMethodId(),
                request.bankAccountId(),
                request.cashRegisterId(),
                request.cashSessionId(),
                currency,
                request.concept(),
                Actors.currentActor(),
                timeProvider.now()
        );
        List<MassVendorPaymentLineRequest> lines = request.lines() == null ? List.of() : request.lines();
        for (MassVendorPaymentLineRequest line : lines) {
            String lineCurrency = line.currency() == null || line.currency().isBlank()
                    ? currency
                    : line.currency();
            batch.addLine(
                    line.partnerId(),
                    line.payableDocumentId(),
                    Money.of(line.amount(), lineCurrency),
                    line.concept()
            );
        }
        return repository.save(batch);
    }

    @Transactional
    public MassVendorPayment confirm(Long id) {
        MassVendorPayment batch = require(id);
        batch.confirm(timeProvider.now());
        return repository.save(batch);
    }

    @Transactional
    public MassVendorPayment post(Long id) {
        MassVendorPayment batch = require(id);
        PaymentMethodConfig method = paymentMethodService.require(batch.getPaymentMethodId());
        CashChannel channel = batch.getBankAccountId() != null ? CashChannel.BANK : CashChannel.CASH_REGISTER;
        if (channel == CashChannel.CASH_REGISTER && batch.getCashRegisterId() == null) {
            throw new DomainException("El pago en efectivo exige caja");
        }
        for (MassVendorPaymentLine line : batch.getLines()) {
            String concept = line.getConcept() == null || line.getConcept().isBlank()
                    ? "Pago proveedor partner " + line.getPartnerId() + " lote " + batch.getReference()
                    : line.getConcept();
            CashMovement movement = CashMovement.record(
                    CashMovementType.EXPENSE,
                    line.amount(),
                    channel,
                    method,
                    batch.getCashRegisterId(),
                    batch.getCashSessionId(),
                    batch.getBankAccountId(),
                    concept,
                    batch.getReference(),
                    TreasuryPolicy.SOURCE_MASS_PAYMENT,
                    batch.getId(),
                    Actors.currentActor(),
                    timeProvider.now()
            );
            cashMovementService.save(movement);
        }
        batch.markPosted(timeProvider.now());
        return repository.save(batch);
    }

    @Transactional
    public MassVendorPayment cancel(Long id) {
        MassVendorPayment batch = require(id);
        batch.cancel(timeProvider.now());
        return repository.save(batch);
    }
}
