package com.valeerp.project;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Parte de horas. Libro: confirmación y anulación por estado, no delete.
 */
@Entity
@Table(name = "project_timesheets")
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hours;

    @Column(name = "billable_amount", precision = 19, scale = 2)
    private BigDecimal billableAmount;

    @Column(length = 3)
    private String currency;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TimesheetStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected TimesheetEntry() {
    }

    static TimesheetEntry draft(Project project, Long taskId, Long employeeId, Long userId,
                                LocalDate workDate, BigDecimal hours, Money billable,
                                String description, Instant createdAt) {
        if (project == null) {
            throw new DomainException("El parte de horas exige un proyecto");
        }
        if (workDate == null) {
            throw new DomainException("La fecha del parte es obligatoria");
        }
        if (employeeId == null && userId == null) {
            throw new DomainException("El parte exige employeeId o userId");
        }
        TimesheetEntry entry = new TimesheetEntry();
        entry.project = project;
        entry.taskId = taskId;
        entry.employeeId = employeeId;
        entry.userId = userId;
        entry.workDate = workDate;
        entry.applyHours(hours);
        entry.applyBillable(billable);
        entry.description = blankToNull(description);
        entry.status = TimesheetStatus.DRAFT;
        entry.createdAt = createdAt;
        return entry;
    }

    void revise(LocalDate workDate, BigDecimal hours, Money billable, String description) {
        assertDraft();
        if (workDate != null) {
            this.workDate = workDate;
        }
        if (hours != null) {
            applyHours(hours);
        }
        if (billable != null) {
            applyBillable(billable);
        }
        if (description != null) {
            this.description = blankToNull(description);
        }
    }

    void confirm(Instant when) {
        assertDraft();
        this.status = TimesheetStatus.CONFIRMED;
        this.confirmedAt = when;
        this.cancelledAt = null;
    }

    void cancel(Instant when) {
        if (status == TimesheetStatus.CANCELLED) {
            throw new DomainException("El parte de horas ya está anulado");
        }
        if (status == TimesheetStatus.DRAFT) {
            this.status = TimesheetStatus.CANCELLED;
            this.cancelledAt = when;
            return;
        }
        this.status = TimesheetStatus.CANCELLED;
        this.cancelledAt = when;
    }

    public Money billableMoney() {
        if (billableAmount == null || currency == null) {
            return null;
        }
        return Money.of(billableAmount, currency);
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return project == null ? null : project.getId();
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public BigDecimal getBillableAmount() {
        return billableAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    private void assertDraft() {
        if (!status.isMutable()) {
            throw new DomainException("Solo un parte en borrador admite cambios (" + status.name() + ")");
        }
    }

    private void applyHours(BigDecimal hours) {
        BigDecimal normalized = ProjectPolicy.normalizeHours(hours);
        if (normalized.compareTo(ProjectPolicy.MIN_TIMESHEET_HOURS) < 0) {
            throw new DomainException("Las horas del parte deben ser al menos " + ProjectPolicy.MIN_TIMESHEET_HOURS);
        }
        this.hours = normalized;
    }

    private void applyBillable(Money billable) {
        if (billable == null) {
            this.billableAmount = null;
            this.currency = null;
            return;
        }
        if (billable.isNegative()) {
            throw new DomainException("El monto facturable no puede ser negativo");
        }
        this.billableAmount = billable.amount();
        this.currency = billable.currency();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TimesheetEntry entry)) {
            return false;
        }
        return id != null && id.equals(entry.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
