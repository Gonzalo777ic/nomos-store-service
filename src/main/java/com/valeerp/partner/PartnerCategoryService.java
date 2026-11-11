package com.valeerp.partner;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartnerCategoryService {

    private final PartnerCategoryRepository repository;
    private final TimeProvider timeProvider;

    public PartnerCategoryService(PartnerCategoryRepository repository, TimeProvider timeProvider) {
        this.repository = repository;
        this.timeProvider = timeProvider;
    }

    public List<PartnerCategory> list() {
        return repository.findAll();
    }

    public PartnerCategory require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Categoría no encontrada"));
    }

    @Transactional
    public PartnerCategory create(PartnerCategoryRequest request) {
        String code = request.code() == null ? "" : request.code().trim().toUpperCase();
        if (repository.existsByCode(code)) {
            throw new DomainException("Ya existe una categoría con código " + code);
        }
        return repository.save(PartnerCategory.create(request.code(), request.name(), request.description()));
    }

    @Transactional
    public PartnerCategory update(Long id, PartnerCategoryRequest request) {
        PartnerCategory category = require(id);
        category.rename(request.name(), request.description());
        return repository.save(category);
    }

    @Transactional
    public void retire(Long id) {
        PartnerCategory category = require(id);
        category.retire(Actors.currentActor(), timeProvider.now());
        repository.save(category);
    }
}
