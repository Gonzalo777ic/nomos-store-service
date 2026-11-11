package com.valeerp.accounting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long> {

    List<AccountingPeriod> findByCompanyIdOrderByStartDateDesc(Long companyId);

    Optional<AccountingPeriod> findByCompanyIdAndCode(Long companyId, String code);

    List<AccountingPeriod> findByCompanyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long companyId, LocalDate onOrBefore, LocalDate onOrAfter);
}
