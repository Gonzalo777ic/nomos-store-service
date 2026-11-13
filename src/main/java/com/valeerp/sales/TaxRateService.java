package com.valeerp.sales;

import com.valeerp.store.service.model.TaxRate;
import com.valeerp.store.service.repository.TaxRateRepository;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaxRateService {

    private final TaxRateRepository taxRateRepository;
    private final TimeProvider timeProvider;

    public TaxRateService(TaxRateRepository taxRateRepository, TimeProvider timeProvider) {
        this.taxRateRepository = taxRateRepository;
        this.timeProvider = timeProvider;
    }

    public List<TaxRate> listActive() {
        return taxRateRepository.findAll();
    }

    public TaxRate require(Long id) {
        return taxRateRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Tasa de impuesto no encontrada"));
    }

    public TaxRate requireByCatalogCode(String code) {
        String resolved = code == null || code.isBlank() ? SalesPolicy.DEFAULT_TAX_CATALOG_CODE : code.trim();
        return taxRateRepository.findByName(resolved).stream()
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE,
                        "Tasa fiscal no encontrada en catálogo: " + resolved));
    }

    @Transactional
    public TaxRate create(TaxRateRequest request) {
        TaxKind kind = TaxKind.require(request.kind() == null ? TaxKind.IGV.name() : request.kind());
        TaxRate taxRate = TaxRate.create(request.name(), kind, request.rate());
        return taxRateRepository.save(taxRate);
    }

    @Transactional
    public TaxRate update(Long id, TaxRateRequest request) {
        TaxRate taxRate = require(id);
        if (request.name() != null && !request.name().isBlank()) {
            taxRate.rename(request.name());
        }
        if (request.kind() != null && !request.kind().isBlank()) {
            taxRate.changeKind(TaxKind.require(request.kind()));
        }
        taxRate.changeRate(request.rate());
        return taxRateRepository.save(taxRate);
    }

    @Transactional
    public void retire(Long id) {
        TaxRate taxRate = require(id);
        taxRate.retire(Actors.currentActor(), timeProvider.now());
        taxRateRepository.save(taxRate);
    }
}
