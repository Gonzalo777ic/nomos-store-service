package com.valeerp.accounting;

import java.time.Instant;
import java.time.LocalDate;

public record AccountingPeriodResponse(
        Long id,
        Long companyId,
        String code,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Instant closedAt,
        String closedBy
) {

    public static AccountingPeriodResponse from(AccountingPeriod period) {
        return new AccountingPeriodResponse(
                period.getId(),
                period.getCompanyId(),
                period.getCode(),
                period.getStartDate(),
                period.getEndDate(),
                period.getStatus().name(),
                period.getClosedAt(),
                period.getClosedBy()
        );
    }
}
