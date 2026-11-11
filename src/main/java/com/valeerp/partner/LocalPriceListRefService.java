package com.valeerp.partner;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocalPriceListRefService {

    private final LocalPriceListRefRepository repository;
    private final TimeProvider timeProvider;

    public LocalPriceListRefService(LocalPriceListRefRepository repository, TimeProvider timeProvider) {
        this.repository = repository;
        this.timeProvider = timeProvider;
    }

    public List<LocalPriceListRef> list() {
        return repository.findAll();
    }

    public LocalPriceListRef require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE, "Lista de precios local no encontrada"));
    }

    @Transactional
    public LocalPriceListRef create(LocalPriceListRefRequest request) {
        String code = request.code() == null ? "" : request.code().trim().toUpperCase();
        if (repository.existsByCode(code)) {
            throw new DomainException("Ya existe una lista de precios local con código " + code);
        }
        return repository.save(LocalPriceListRef.create(
                request.catalogPriceListId(), request.code(), request.name()));
    }

    @Transactional
    public LocalPriceListRef update(Long id, LocalPriceListRefRequest request) {
        LocalPriceListRef ref = require(id);
        ref.rename(request.name());
        ref.bindCatalogId(request.catalogPriceListId());
        return repository.save(ref);
    }

    @Transactional
    public void retire(Long id) {
        LocalPriceListRef ref = require(id);
        ref.retire(Actors.currentActor(), timeProvider.now());
        repository.save(ref);
    }
}
