package com.valeerp.accounting;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JournalService {

    private final JournalRepository journalRepository;
    private final ChartAccountRepository chartAccountRepository;
    private final TimeProvider timeProvider;

    public JournalService(JournalRepository journalRepository,
                          ChartAccountRepository chartAccountRepository,
                          TimeProvider timeProvider) {
        this.journalRepository = journalRepository;
        this.chartAccountRepository = chartAccountRepository;
        this.timeProvider = timeProvider;
    }

    public Page<Journal> list(Long companyId, JournalKind kind, Pageable pageable) {
        requireCompanyId(companyId);
        if (kind == null) {
            return journalRepository.findByCompanyId(companyId, pageable);
        }
        return journalRepository.findByCompanyIdAndKind(companyId, kind, pageable);
    }

    public Journal require(Long id) {
        return journalRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Diario no encontrado"));
    }

    public Journal requireByKind(Long companyId, JournalKind kind) {
        requireCompanyId(companyId);
        if (kind == null) {
            throw new DomainException("El tipo de diario es obligatorio");
        }
        return journalRepository.findByCompanyIdAndKindOrderByCodeAsc(companyId, kind).stream()
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE,
                        "Diario no configurado para " + kind.name()));
    }

    @Transactional
    public Journal create(JournalCreateRequest request) {
        requireCompanyId(request.companyId());
        String code = Journal.normalizeCode(request.code());
        ensureCodeAvailable(request.companyId(), code, null);
        Long defaultAccountId = validateDefaultAccount(request.companyId(), request.defaultAccountId());
        JournalKind kind = resolveKind(request.kind(), request.journalType());
        Journal journal = Journal.open(
                request.companyId(),
                code,
                request.name(),
                kind,
                defaultAccountId,
                timeProvider.now()
        );
        return journalRepository.save(journal);
    }

    @Transactional
    public Journal update(Long id, JournalUpdateRequest request) {
        Journal journal = require(id);
        String code = Journal.normalizeCode(request.code());
        ensureCodeAvailable(journal.getCompanyId(), code, id);
        Long defaultAccountId = validateDefaultAccount(journal.getCompanyId(), request.defaultAccountId());
        JournalKind kind = resolveKind(request.kind(), request.journalType());
        journal.reconfigure(code, request.name(), kind, defaultAccountId);
        return journalRepository.save(journal);
    }

    @Transactional
    public void retire(Long id) {
        Journal journal = require(id);
        journal.archive(Actors.currentActor(), timeProvider.now());
        journalRepository.save(journal);
    }

    private Long validateDefaultAccount(Long companyId, Long accountId) {
        if (accountId == null) {
            return null;
        }
        ChartAccount account = chartAccountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Cuenta contable no encontrada"));
        if (!account.getCompanyId().equals(companyId)) {
            throw new DomainException("La cuenta por defecto debe pertenecer a la misma compañía");
        }
        if (!account.isAllowsPosting()) {
            throw new DomainException("La cuenta por defecto del diario debe ser imputable");
        }
        return accountId;
    }

    private void ensureCodeAvailable(Long companyId, String code, Long excludingId) {
        journalRepository.findByCompanyIdAndCode(companyId, code).ifPresent(existing -> {
            if (excludingId == null || !excludingId.equals(existing.getId())) {
                throw new DomainException("Ya existe un diario activo con código " + code);
            }
        });
    }

    private static JournalKind resolveKind(String kind, String journalType) {
        if (kind != null && !kind.isBlank()) {
            return JournalKind.require(kind);
        }
        return JournalKind.require(journalType);
    }

    private static void requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
    }
}
