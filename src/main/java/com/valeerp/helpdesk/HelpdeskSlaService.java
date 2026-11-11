package com.valeerp.helpdesk;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class HelpdeskSlaService {

    private final HelpdeskSlaRepository slaRepository;
    private final TimeProvider timeProvider;

    public HelpdeskSlaService(HelpdeskSlaRepository slaRepository, TimeProvider timeProvider) {
        this.slaRepository = slaRepository;
        this.timeProvider = timeProvider;
    }

    public Page<HelpdeskSla> list(Pageable pageable) {
        return slaRepository.findAll(pageable);
    }

    public HelpdeskSla require(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "SLA no encontrado"));
    }

    @Transactional
    public HelpdeskSla create(SlaCreateRequest request) {
        ensureCodeAvailable(request.code(), null);
        HelpdeskSla sla = HelpdeskSla.open(
                request.code(),
                request.name(),
                request.description(),
                request.responseHours(),
                request.resolutionHours(),
                timeProvider.now()
        );
        return slaRepository.save(sla);
    }

    @Transactional
    public HelpdeskSla update(Long id, SlaUpdateRequest request) {
        HelpdeskSla sla = require(id);
        ensureCodeAvailable(request.code(), id);
        sla.rename(
                request.code(),
                request.name(),
                request.description(),
                request.responseHours(),
                request.resolutionHours()
        );
        return slaRepository.save(sla);
    }

    @Transactional
    public void retire(Long id) {
        HelpdeskSla sla = require(id);
        sla.archive(Actors.currentActor(), timeProvider.now());
        slaRepository.save(sla);
    }

    private void ensureCodeAvailable(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de SLA es obligatorio");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        slaRepository.findByCode(normalized)
                .filter(existing -> excludeId == null || !excludeId.equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe un SLA con código " + normalized);
                });
    }
}
