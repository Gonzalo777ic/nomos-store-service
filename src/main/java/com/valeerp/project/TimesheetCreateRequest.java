package com.valeerp.project;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimesheetCreateRequest(
        Long taskId,
        Long employeeId,
        Long userId,
        LocalDate workDate,
        BigDecimal hours,
        BigDecimal billableAmount,
        String currency,
        String description
) {
}
