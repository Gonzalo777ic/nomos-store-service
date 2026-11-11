package com.valeerp.project;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimesheetUpdateRequest(
        LocalDate workDate,
        BigDecimal hours,
        BigDecimal billableAmount,
        String currency,
        String description
) {
}
