package com.valeerp.partner;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentTermService {

    private final PaymentTermRepository repository;
    private final TimeProvider timeProvider;

    public PaymentTermService(PaymentTermRepository repository, TimeProvider timeProvider) {
        this.repository = repository;
        this.timeProvider = timeProvider;
    }

    public List<PaymentTerm> list() {
        return repository.findAll();
    }

    public PaymentTerm require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Condición de pago no encontrada"));
    }

    @Transactional
    public PaymentTerm create(PaymentTermRequest request) {
        if (request.daysDue() == null) {
            throw new DomainException("Los días de crédito son obligatorios");
        }
        String code = request.code() == null ? "" : request.code().trim().toUpperCase();
        if (repository.existsByCode(code)) {
            throw new DomainException("Ya existe una condición de pago con código " + code);
        }
        return repository.save(PaymentTerm.create(
                request.code(), request.name(), request.daysDue(), request.description()));
    }

    @Transactional
    public PaymentTerm update(Long id, PaymentTermRequest request) {
        if (request.daysDue() == null) {
            throw new DomainException("Los días de crédito son obligatorios");
        }
        PaymentTerm term = require(id);
        term.rename(request.name(), request.daysDue(), request.description());
        return repository.save(term);
    }

    @Transactional
    public void retire(Long id) {
        PaymentTerm term = require(id);
        term.retire(Actors.currentActor(), timeProvider.now());
        repository.save(term);
    }
}
