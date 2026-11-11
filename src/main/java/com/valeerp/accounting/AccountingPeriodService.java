package com.valeerp.accounting;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountingPeriodService {

    private final AccountingPeriodRepository periodRepository;
    private final TimeProvider timeProvider;

    public AccountingPeriodService(AccountingPeriodRepository periodRepository, TimeProvider timeProvider) {
        this.periodRepository = periodRepository;
        this.timeProvider = timeProvider;
    }

    public List<AccountingPeriod> list(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        return periodRepository.findByCompanyIdOrderByStartDateDesc(companyId);
    }

    public AccountingPeriod require(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Periodo no encontrado"));
    }

    @Transactional
    public AccountingPeriod open(AccountingPeriodCreateRequest request) {
        if (request == null) {
            throw new DomainException("La solicitud del periodo es obligatoria");
        }
        periodRepository.findByCompanyIdAndCode(request.companyId(),
                        request.code() == null ? "" : request.code().trim().toUpperCase())
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe un periodo con código " + existing.getCode());
                });
        AccountingPeriod period = AccountingPeriod.open(
                request.companyId(),
                request.code(),
                request.startDate(),
                request.endDate()
        );
        return periodRepository.save(period);
    }

    /**
     * Cierre de periodo (#78): bloquea diarios para fechas del rango cerrado.
     */
    @Transactional
    public AccountingPeriod close(Long id) {
        AccountingPeriod period = require(id);
        period.close(Actors.currentActor(), timeProvider.now());
        return periodRepository.save(period);
    }
}
