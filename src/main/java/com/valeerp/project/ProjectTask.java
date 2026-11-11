package com.valeerp.project;

import com.valeerp.kernel.DomainException;
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

/**
 * Tarea del proyecto. Documento con estados; no soft-delete.
 */
@Entity
@Table(name = "project_tasks")
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TaskStatus status;

    @Column(name = "assignee_user_id")
    private Long assigneeUserId;

    @Column(name = "planned_hours", nullable = false, precision = 12, scale = 2)
    private BigDecimal plannedHours;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected ProjectTask() {
    }

    static ProjectTask open(Project project, String name, String description, Long assigneeUserId,
                            BigDecimal plannedHours, Instant createdAt) {
        if (project == null) {
            throw new DomainException("La tarea exige un proyecto");
        }
        ProjectTask task = new ProjectTask();
        task.project = project;
        task.applyName(name);
        task.description = blankToNull(description);
        task.status = TaskStatus.TODO;
        task.assigneeUserId = assigneeUserId;
        task.plannedHours = requireNonNegativeHours(plannedHours);
        task.createdAt = createdAt;
        return task;
    }

    void rename(String name, String description, Long assigneeUserId, BigDecimal plannedHours) {
        assertOpen();
        applyName(name);
        this.description = blankToNull(description);
        this.assigneeUserId = assigneeUserId;
        if (plannedHours != null) {
            this.plannedHours = requireNonNegativeHours(plannedHours);
        }
    }

    void start(Instant when) {
        assertOpen();
        if (status == TaskStatus.IN_PROGRESS) {
            return;
        }
        this.status = TaskStatus.IN_PROGRESS;
        this.closedAt = null;
        if (when == null) {
            throw new DomainException("La fecha de inicio es obligatoria");
        }
    }

    void complete(Instant when) {
        assertOpen();
        this.status = TaskStatus.DONE;
        this.closedAt = when;
    }

    void cancel(Instant when) {
        assertOpen();
        this.status = TaskStatus.CANCELLED;
        this.closedAt = when;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return project == null ? null : project.getId();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public BigDecimal getPlannedHours() {
        return plannedHours;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    private void assertOpen() {
        if (status.isTerminal()) {
            throw new DomainException("La tarea ya está cerrada (" + status.name() + ")");
        }
    }

    private void applyName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la tarea es obligatorio");
        }
        this.name = name.trim();
    }

    private static BigDecimal requireNonNegativeHours(BigDecimal hours) {
        BigDecimal normalized = ProjectPolicy.normalizeHours(hours == null ? BigDecimal.ZERO : hours);
        if (normalized.signum() < 0) {
            throw new DomainException("Las horas planificadas no pueden ser negativas");
        }
        return normalized;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProjectTask task)) {
            return false;
        }
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
