package com.valeerp.treasury;

import com.nomos.store.service.model.PaymentMethodConfig;
import com.nomos.store.service.repository.PaymentMethodConfigRepository;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentMethodService {

    private final PaymentMethodConfigRepository repository;
    private final TimeProvider timeProvider;

    public PaymentMethodService(PaymentMethodConfigRepository repository, TimeProvider timeProvider) {
        this.repository = repository;
        this.timeProvider = timeProvider;
    }

    public List<PaymentMethodConfig> list() {
        return repository.findAll();
    }

    public PaymentMethodConfig require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE, "Método de pago no encontrado"));
    }

    @Transactional
    public PaymentMethodConfig create(PaymentMethodCreateRequest request) {
        String code = PaymentMethodConfig.normalizeCode(request.code());
        if (repository.existsByCode(code)) {
            throw new DomainException("Ya existe un método de pago con código " + code);
        }
        PaymentMethodKind kind = PaymentMethodKind.require(request.kind());
        boolean requiresReference = Boolean.TRUE.equals(request.requiresReference());
        return repository.save(PaymentMethodConfig.create(code, request.name(), kind, requiresReference));
    }

    @Transactional
    public PaymentMethodConfig update(Long id, PaymentMethodUpdateRequest request) {
        PaymentMethodConfig method = require(id);
        String code = PaymentMethodConfig.normalizeCode(request.code());
        repository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe un método de pago con código " + code);
                });
        method.rename(
                code,
                request.name(),
                PaymentMethodKind.require(request.kind()),
                Boolean.TRUE.equals(request.requiresReference())
        );
        return repository.save(method);
    }

    @Transactional
    public void retire(Long id) {
        PaymentMethodConfig method = require(id);
        method.archive(Actors.currentActor(), timeProvider.now());
        repository.save(method);
    }
}
