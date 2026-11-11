package com.valeerp.crm;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Actividad CRM ligada a una oportunidad. Libro: complete/cancel por estado; sin delete físico.
 */
@Entity
@Table(name = "crm_activities")
public class CrmActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "opportunity_id", nullable = false)
    private Long opportunityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ActivityStatus status;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(length = 2000)
    private String notes;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason", length = 400)
    private String cancelReason;

    protected CrmActivity() {
    }

    public static CrmActivity schedule(Long opportunityId, ActivityType type, String subject,
                                       String notes, Instant dueAt, Instant createdAt) {
        if (opportunityId == null) {
            throw new DomainException("La actividad exige una oportunidad");
        }
        if (type == null) {
            throw new DomainException("El tipo de actividad es obligatorio");
        }
        CrmActivity activity = new CrmActivity();
        activity.opportunityId = opportunityId;
        activity.type = type;
        activity.status = ActivityStatus.PLANNED;
        activity.applySubject(subject);
        activity.notes = blankToNull(notes);
        activity.dueAt = dueAt;
        activity.createdAt = createdAt;
        return activity;
    }

    public void reschedule(String subject, String notes, Instant dueAt) {
        assertPlanned();
        applySubject(subject);
        this.notes = blankToNull(notes);
        this.dueAt = dueAt;
    }

    public void complete(Instant when, String completionNotes) {
        assertPlanned();
        if (when == null) {
            throw new DomainException("La fecha de completado es obligatoria");
        }
        this.status = ActivityStatus.COMPLETED;
        this.completedAt = when;
        if (completionNotes != null && !completionNotes.isBlank()) {
            this.notes = completionNotes.trim();
        }
    }

    public void cancel(Instant when, String reason) {
        assertPlanned();
        if (when == null) {
            throw new DomainException("La fecha de cancelación es obligatoria");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("El motivo de cancelación es obligatorio");
        }
        this.status = ActivityStatus.CANCELLED;
        this.cancelledAt = when;
        this.cancelReason = reason.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public ActivityType getType() {
        return type;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    private void assertPlanned() {
        if (!status.isOpen()) {
            throw new DomainException("La actividad ya está cerrada (" + status.name() + ")");
        }
    }

    private void applySubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new DomainException("El asunto de la actividad es obligatorio");
        }
        this.subject = subject.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CrmActivity activity)) {
            return false;
        }
        return id != null && id.equals(activity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
