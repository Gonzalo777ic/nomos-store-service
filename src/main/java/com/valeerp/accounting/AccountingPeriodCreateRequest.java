package com.valeerp.accounting;

import java.time.LocalDate;

public record AccountingPeriodCreateRequest(
        Long companyId,
        String code,
        LocalDate startDate,
        LocalDate endDate
) {
}
