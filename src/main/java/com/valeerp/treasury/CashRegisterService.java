package com.valeerp.treasury;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashRegisterService {

    private final CashRegisterRepository cashRegisterRepository;
    private final TimeProvider timeProvider;

    public CashRegisterService(CashRegisterRepository cashRegisterRepository, TimeProvider timeProvider) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.timeProvider = timeProvider;
    }

    public Page<CashRegister> list(Long companyId, Pageable pageable) {
        requireCompanyId(companyId);
        return cashRegisterRepository.findByCompanyId(companyId, pageable);
    }

    public CashRegister require(Long id) {
        return cashRegisterRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Caja no encontrada"));
    }

    @Transactional
    public CashRegister create(CashRegisterCreateRequest request) {
        requireCompanyId(request.companyId());
        String code = CashRegister.normalizeCode(request.code());
        if (cashRegisterRepository.existsByCompanyIdAndCode(request.companyId(), code)) {
            throw new DomainException("Ya existe una caja con código " + code);
        }
        String currency = request.currency() == null || request.currency().isBlank()
                ? TreasuryPolicy.DEFAULT_CURRENCY.code()
                : request.currency();
        CashRegister register = CashRegister.open(
                request.companyId(),
                code,
                request.name(),
                request.journalId(),
                currency,
                timeProvider.now()
        );
        return cashRegisterRepository.save(register);
    }

    @Transactional
    public CashRegister update(Long id, CashRegisterUpdateRequest request) {
        CashRegister register = require(id);
        String code = CashRegister.normalizeCode(request.code());
        cashRegisterRepository.findByCompanyIdAndCode(register.getCompanyId(), code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe una caja con código " + code);
                });
        String currency = request.currency() == null || request.currency().isBlank()
                ? register.getCurrencyCode()
                : request.currency();
        register.rename(code, request.name(), request.journalId(), currency);
        return cashRegisterRepository.save(register);
    }

    @Transactional
    public void retire(Long id) {
        CashRegister register = require(id);
        register.archive(Actors.currentActor(), timeProvider.now());
        cashRegisterRepository.save(register);
    }

    private static void requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
    }
}
