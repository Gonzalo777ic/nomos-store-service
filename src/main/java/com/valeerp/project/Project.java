package com.valeerp.project;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Proyecto maestro. Soft-delete al archivar; tareas y partes viven como documentos con estados.
 */
@Entity
@Table(name = "projects")
public class Project extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "manager_user_id")
    private Long managerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ProjectStatus status;

    @Column(name = "planned_budget", nullable = false, precision = 19, scale = 2)
    private BigDecimal plannedBudget;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<ProjectTask> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @OrderBy("workDate ASC, createdAt ASC")
    private List<TimesheetEntry> timesheets = new ArrayList<>();

    protected Project() {
    }

    public static Project open(String code, String name, String description, Long partnerId,
                               Long managerUserId, Money plannedBudget, Instant createdAt) {
        Project project = new Project();
        project.applyIdentity(code, name, description);
        project.partnerId = partnerId;
        project.managerUserId = managerUserId;
        project.status = ProjectStatus.DRAFT;
        project.applyBudget(plannedBudget);
        project.createdAt = createdAt;
        return project;
    }

    public void rename(String code, String name, String description, Long partnerId, Long managerUserId) {
        assertNotTerminal();
        applyIdentity(code, name, description);
        this.partnerId = partnerId;
        this.managerUserId = managerUserId;
    }

    public void changeBudget(Money plannedBudget) {
        assertNotTerminal();
        applyBudget(plannedBudget);
    }

    public void activate(Instant when) {
        if (status == ProjectStatus.OPEN) {
            return;
        }
        if (status.isTerminal()) {
            throw new DomainException("Un proyecto cerrado no se puede reabrir (" + status.name() + ")");
        }
        this.status = ProjectStatus.OPEN;
        this.closedAt = null;
        if (when == null) {
            throw new DomainException("La fecha de activación es obligatoria");
        }
    }

    public void complete(Instant when) {
        assertOpenForClose();
        this.status = ProjectStatus.DONE;
        this.closedAt = when;
    }

    public void cancel(Instant when) {
        assertOpenForClose();
        this.status = ProjectStatus.CANCELLED;
        this.closedAt = when;
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
    }

    public ProjectTask addTask(String name, String description, Long assigneeUserId,
                               BigDecimal plannedHours, Instant createdAt) {
        assertAllowsPlanning();
        ProjectTask task = ProjectTask.open(this, name, description, assigneeUserId, plannedHours, createdAt);
        tasks.add(task);
        return task;
    }

    public void updateTask(Long taskId, String name, String description, Long assigneeUserId,
                           BigDecimal plannedHours) {
        assertAllowsPlanning();
        requireTask(taskId).rename(name, description, assigneeUserId, plannedHours);
    }

    public void startTask(Long taskId, Instant when) {
        assertAllowsWork();
        requireTask(taskId).start(when);
    }

    public void completeTask(Long taskId, Instant when) {
        assertAllowsWork();
        requireTask(taskId).complete(when);
    }

    public void cancelTask(Long taskId, Instant when) {
        assertAllowsPlanning();
        requireTask(taskId).cancel(when);
    }

    public TimesheetEntry logTimesheet(Long taskId, Long employeeId, Long userId, LocalDate workDate,
                                       BigDecimal hours, Money billable, String description,
                                       Instant createdAt) {
        assertAllowsWork();
        if (taskId != null) {
            requireTask(taskId);
        }
        Money resolvedBillable = requireProjectCurrency(billable);
        TimesheetEntry entry = TimesheetEntry.draft(
                this, taskId, employeeId, userId, workDate, hours, resolvedBillable, description, createdAt);
        timesheets.add(entry);
        return entry;
    }

    public void reviseTimesheet(Long timesheetId, LocalDate workDate, BigDecimal hours,
                                Money billable, String description) {
        assertAllowsWork();
        Money resolvedBillable = billable == null ? null : requireProjectCurrency(billable);
        requireTimesheet(timesheetId).revise(workDate, hours, resolvedBillable, description);
    }

    public void confirmTimesheet(Long timesheetId, Instant when) {
        assertAllowsWork();
        requireTimesheet(timesheetId).confirm(when);
    }

    public void cancelTimesheet(Long timesheetId, Instant when) {
        requireTimesheet(timesheetId).cancel(when);
    }

    public Money plannedBudgetMoney() {
        return Money.of(plannedBudget, currency);
    }

    public BigDecimal confirmedHours() {
        return timesheets.stream()
                .filter(entry -> entry.getStatus().countsTowardTotals())
                .map(TimesheetEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ProjectPolicy.HOURS_SCALE, ProjectPolicy.HOURS_ROUNDING);
    }

    public Money confirmedBillableTotal() {
        Money total = Money.zero(currency);
        for (TimesheetEntry entry : timesheets) {
            if (!entry.getStatus().countsTowardTotals()) {
                continue;
            }
            Money billable = entry.billableMoney();
            if (billable != null) {
                total = total.plus(billable);
            }
        }
        return total;
    }

    public BigDecimal plannedTaskHours() {
        return tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .map(ProjectTask::getPlannedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ProjectPolicy.HOURS_SCALE, ProjectPolicy.HOURS_ROUNDING);
    }

    public ProjectTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new DomainException("La tarea es obligatoria");
        }
        return tasks.stream()
                .filter(task -> taskId.equals(task.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Tarea no encontrada"));
    }

    public TimesheetEntry requireTimesheet(Long timesheetId) {
        if (timesheetId == null) {
            throw new DomainException("El parte de horas es obligatorio");
        }
        return timesheets.stream()
                .filter(entry -> timesheetId.equals(entry.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Parte de horas no encontrado"));
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getManagerUserId() {
        return managerUserId;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public BigDecimal getPlannedBudget() {
        return plannedBudget;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public List<ProjectTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public List<TimesheetEntry> getTimesheets() {
        return Collections.unmodifiableList(timesheets);
    }

    private void assertAllowsPlanning() {
        if (isRetired()) {
            throw new DomainException("El proyecto está dado de baja");
        }
        if (!status.allowsPlanning()) {
            throw new DomainException("El proyecto no admite planificación en estado " + status.name());
        }
    }

    private void assertAllowsWork() {
        if (isRetired()) {
            throw new DomainException("El proyecto está dado de baja");
        }
        if (!status.allowsWork()) {
            throw new DomainException("El proyecto no admite partes de horas en estado " + status.name());
        }
    }

    private Money requireProjectCurrency(Money billable) {
        if (billable == null) {
            return null;
        }
        if (!currency.equals(billable.currency())) {
            throw new DomainException("El monto facturable debe usar la moneda del proyecto (" + currency + ")");
        }
        return billable;
    }

    private void assertNotTerminal() {
        if (isRetired()) {
            throw new DomainException("El proyecto está dado de baja");
        }
        if (status.isTerminal()) {
            throw new DomainException("El proyecto ya está cerrado (" + status.name() + ")");
        }
    }

    private void assertOpenForClose() {
        if (status != ProjectStatus.OPEN && status != ProjectStatus.DRAFT) {
            throw new DomainException("El proyecto ya está cerrado (" + status.name() + ")");
        }
    }

    private void applyIdentity(String code, String name, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de proyecto es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de proyecto es obligatorio");
        }
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.name = name.trim();
        this.description = blankToNull(description);
    }

    private void applyBudget(Money plannedBudget) {
        Money budget = plannedBudget == null
                ? Money.zero(ProjectPolicy.DEFAULT_CURRENCY.code())
                : plannedBudget;
        if (budget.isNegative()) {
            throw new DomainException("El presupuesto planificado no puede ser negativo");
        }
        this.plannedBudget = budget.amount();
        this.currency = budget.currency();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Project project)) {
            return false;
        }
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
