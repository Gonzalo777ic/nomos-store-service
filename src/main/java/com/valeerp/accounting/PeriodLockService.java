package com.valeerp.accounting;

import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Guardián de cierre de periodo (#78): si la fecha cae en un periodo CLOSED,
 * los diarios de esa compañía quedan bloqueados para post/void.
 */
@Service
public class PeriodLockService {

    private final AccountingPeriodRepository periodRepository;

    public PeriodLockService(AccountingPeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    public void assertJournalsOpen(Long companyId, LocalDate entryDate) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria para validar el periodo");
        }
        if (entryDate == null) {
            throw new DomainException("La fecha del asiento es obligatoria");
        }
        List<AccountingPeriod> covering = periodRepository
                .findByCompanyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        companyId, entryDate, entryDate);
        for (AccountingPeriod period : covering) {
            period.assertOpenFor(entryDate);
        }
    }
}
