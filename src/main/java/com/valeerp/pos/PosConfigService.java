package com.valeerp.pos;

import com.valeerp.accounting.Journal;
import com.valeerp.accounting.JournalRepository;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.partner.LocalPriceListRefRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PosConfigService {

    private final PosConfigRepository configRepository;
    private final LocalPriceListRefRepository priceListRefRepository;
    private final JournalRepository journalRepository;
    private final TimeProvider timeProvider;

    public PosConfigService(PosConfigRepository configRepository,
                            LocalPriceListRefRepository priceListRefRepository,
                            JournalRepository journalRepository,
                            TimeProvider timeProvider) {
        this.configRepository = configRepository;
        this.priceListRefRepository = priceListRefRepository;
        this.journalRepository = journalRepository;
        this.timeProvider = timeProvider;
    }

    public Page<PosConfig> list(Long companyId, Pageable pageable) {
        if (companyId == null) {
            return configRepository.findAll(pageable);
        }
        return configRepository.findByCompanyId(companyId, pageable);
    }

    public PosConfig require(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Config POS no encontrada"));
    }

    @Transactional
    public PosConfig create(PosConfigCreateRequest request) {
        ensureJournal(request.companyId(), request.journalId());
        ensurePriceList(request.priceListId());
        ensureCodeAvailable(request.companyId(), request.code(), null);
        // cashRegisterId: referencia local a tesorería (#81); sin FK JPA para no romper el maestro de caja.
        PosConfig config = PosConfig.create(
                request.companyId(),
                request.code(),
                request.name(),
                request.cashRegisterId(),
                request.journalId(),
                request.priceListId(),
                request.currency(),
                timeProvider.now()
        );
        return configRepository.save(config);
    }

    @Transactional
    public PosConfig update(Long id, PosConfigUpdateRequest request) {
        PosConfig config = require(id);
        ensureJournal(config.getCompanyId(), request.journalId());
        ensurePriceList(request.priceListId());
        config.rename(request.name());
        config.rebind(request.cashRegisterId(), request.journalId(), request.priceListId());
        config.changeCurrency(request.currency());
        return configRepository.save(config);
    }

    @Transactional
    public void retire(Long id) {
        PosConfig config = require(id);
        config.archive(Actors.currentActor(), timeProvider.now());
        configRepository.save(config);
    }

    private void ensureJournal(Long companyId, Long journalId) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Diario contable no encontrado"));
        if (journal.isRetired()) {
            throw new DomainException("El diario contable está dado de baja");
        }
        if (!journal.getCompanyId().equals(companyId)) {
            throw new DomainException("El diario no pertenece a la compañía del POS");
        }
    }

    private void ensurePriceList(Long priceListId) {
        if (priceListId == null) {
            throw new DomainException("priceListId es obligatorio");
        }
        priceListRefRepository.findById(priceListId)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Lista de precios no encontrada"));
    }

    private void ensureCodeAvailable(Long companyId, String code, Long excludingId) {
        if (companyId == null || code == null || code.isBlank()) {
            return;
        }
        String normalized = code.trim().toUpperCase();
        configRepository.findByCompanyIdAndCode(companyId, normalized).ifPresent(existing -> {
            if (excludingId == null || !existing.getId().equals(excludingId)) {
                throw new DomainException("Ya existe una config POS con código " + normalized);
            }
        });
    }
}
