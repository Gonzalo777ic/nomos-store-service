package com.valeerp.accounting;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChartAccountService {

    private final ChartAccountRepository chartAccountRepository;
    private final TimeProvider timeProvider;

    public ChartAccountService(ChartAccountRepository chartAccountRepository, TimeProvider timeProvider) {
        this.chartAccountRepository = chartAccountRepository;
        this.timeProvider = timeProvider;
    }

    public Page<ChartAccount> listRoots(Long companyId, Pageable pageable) {
        requireCompanyId(companyId);
        return chartAccountRepository.findByCompanyIdAndParentIsNull(companyId, pageable);
    }

    public Page<ChartAccount> listByCompany(Long companyId, Pageable pageable) {
        requireCompanyId(companyId);
        return chartAccountRepository.findByCompanyId(companyId, pageable);
    }

    public List<ChartAccount> listChildren(Long companyId, Long parentId) {
        requireCompanyId(companyId);
        ChartAccount parent = require(parentId);
        ensureSameCompany(parent, companyId);
        return chartAccountRepository.findByCompanyIdAndParentId(companyId, parentId);
    }

    public ChartAccount require(Long id) {
        return chartAccountRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Cuenta contable no encontrada"));
    }

    @Transactional
    public ChartAccount create(ChartAccountCreateRequest request) {
        requireCompanyId(request.companyId());
        String code = ChartAccount.normalizeCode(request.code());
        ensureCodeAvailable(request.companyId(), code, null);
        ChartAccount parent = resolveParent(request.companyId(), request.parentId());
        boolean allowsPosting = request.allowsPosting() == null || request.allowsPosting();
        ChartAccount account = ChartAccount.open(
                request.companyId(),
                code,
                request.name(),
                AccountClass.require(request.accountClass()),
                allowsPosting,
                parent,
                timeProvider.now()
        );
        return chartAccountRepository.save(account);
    }

    @Transactional
    public ChartAccount update(Long id, ChartAccountUpdateRequest request) {
        ChartAccount account = require(id);
        String code = ChartAccount.normalizeCode(request.code());
        ensureCodeAvailable(account.getCompanyId(), code, id);
        ChartAccount parent = resolveParent(account.getCompanyId(), request.parentId());
        boolean allowsPosting = request.allowsPosting() == null
                ? account.isAllowsPosting()
                : request.allowsPosting();
        account.rename(code, request.name(), AccountClass.require(request.accountClass()), allowsPosting);
        account.attachTo(parent);
        return chartAccountRepository.save(account);
    }

    @Transactional
    public void retire(Long id) {
        ChartAccount account = require(id);
        account.archive(Actors.currentActor(), timeProvider.now());
        chartAccountRepository.save(account);
    }

    private ChartAccount resolveParent(Long companyId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        ChartAccount parent = require(parentId);
        ensureSameCompany(parent, companyId);
        return parent;
    }

    private void ensureCodeAvailable(Long companyId, String code, Long excludingId) {
        chartAccountRepository.findByCompanyIdAndCode(companyId, code).ifPresent(existing -> {
            if (excludingId == null || !excludingId.equals(existing.getId())) {
                throw new DomainException("Ya existe una cuenta activa con código " + code);
            }
        });
    }

    private static void ensureSameCompany(ChartAccount account, Long companyId) {
        if (!account.getCompanyId().equals(companyId)) {
            throw new DomainException("La cuenta no pertenece a la compañía indicada");
        }
    }

    private static void requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
    }
}
