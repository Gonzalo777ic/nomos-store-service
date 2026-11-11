package com.valeerp.project;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TimesheetResponse(
        Long id,
        Long projectId,
        Long taskId,
        Long employeeId,
        Long userId,
        LocalDate workDate,
        BigDecimal hours,
        BigDecimal billableAmount,
        String currency,
        String description,
        String status,
        Instant createdAt,
        Instant confirmedAt,
        Instant cancelledAt
) {

    public static TimesheetResponse from(TimesheetEntry entry) {
        return new TimesheetResponse(
                entry.getId(),
                entry.getProjectId(),
                entry.getTaskId(),
                entry.getEmployeeId(),
                entry.getUserId(),
                entry.getWorkDate(),
                entry.getHours(),
                entry.getBillableAmount(),
                entry.getCurrency(),
                entry.getDescription(),
                entry.getStatus().name(),
                entry.getCreatedAt(),
                entry.getConfirmedAt(),
                entry.getCancelledAt()
        );
    }
}
