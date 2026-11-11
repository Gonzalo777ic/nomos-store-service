package com.valeerp.helpdesk;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ticket de helpdesk. Libro con estados; adjuntos son metadatos soft-delete.
 */
@Entity
@Table(name = "helpdesk_tickets")
public class HelpdeskTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TicketPriority priority;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "reporter_user_id")
    private Long reporterUserId;

    @Column(name = "assignee_user_id")
    private Long assigneeUserId;

    @Column(name = "sla_id")
    private Long slaId;

    @Column(name = "sla_response_hours", nullable = false)
    private int slaResponseHours;

    @Column(name = "sla_resolution_hours", nullable = false)
    private int slaResolutionHours;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "first_response_at")
    private Instant firstResponseAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<TicketAttachment> attachments = new ArrayList<>();

    protected HelpdeskTicket() {
    }

    public static HelpdeskTicket open(String subject, String description, TicketPriority priority,
                                      Long partnerId, Long reporterUserId, Long assigneeUserId,
                                      HelpdeskSla sla, Instant createdAt) {
        HelpdeskTicket ticket = new HelpdeskTicket();
        ticket.applySubject(subject);
        ticket.description = blankToNull(description);
        ticket.status = TicketStatus.NEW;
        ticket.priority = priority == null ? TicketPriority.NORMAL : priority;
        ticket.partnerId = partnerId;
        ticket.reporterUserId = reporterUserId;
        ticket.assigneeUserId = assigneeUserId;
        ticket.applySlaSnapshot(sla);
        ticket.createdAt = createdAt;
        return ticket;
    }

    public void update(String subject, String description, TicketPriority priority,
                       Long partnerId, Long assigneeUserId) {
        assertOpen();
        applySubject(subject);
        this.description = blankToNull(description);
        if (priority != null) {
            this.priority = priority;
        }
        this.partnerId = partnerId;
        this.assigneeUserId = assigneeUserId;
    }

    public void assign(Long assigneeUserId) {
        assertOpen();
        this.assigneeUserId = assigneeUserId;
    }

    public void startProgress(Instant when) {
        assertOpen();
        markFirstResponse(when);
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void waitForCustomer(Instant when) {
        assertOpen();
        markFirstResponse(when);
        this.status = TicketStatus.WAITING;
    }

    public void resolve(Instant when) {
        assertOpen();
        markFirstResponse(when);
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = when;
    }

    public void close(Instant when) {
        if (status.isTerminal()) {
            throw new DomainException("El ticket ya está cerrado (" + status.name() + ")");
        }
        if (status != TicketStatus.RESOLVED && status.isOpen()) {
            markFirstResponse(when);
            this.resolvedAt = when;
        }
        this.status = TicketStatus.CLOSED;
        this.closedAt = when;
    }

    public void cancel(Instant when) {
        assertOpen();
        this.status = TicketStatus.CANCELLED;
        this.closedAt = when;
    }

    public void reapplySla(HelpdeskSla sla) {
        assertOpen();
        applySlaSnapshot(sla);
    }

    public TicketAttachment attach(String label, String url, String contentType, Long sizeBytes,
                                   String fileName, Instant createdAt) {
        assertNotCancelled();
        TicketAttachment attachment = TicketAttachment.attach(
                this, label, url, contentType, sizeBytes, fileName, createdAt);
        attachments.add(attachment);
        return attachment;
    }

    public void retireAttachment(Long attachmentId, Actor actor, Instant when) {
        requireActiveAttachment(attachmentId).archive(actor, when);
    }

    public boolean isResponseBreached(Instant now) {
        if (firstResponseAt != null) {
            return firstResponseAt.isAfter(responseDeadline());
        }
        return now != null && now.isAfter(responseDeadline());
    }

    public boolean isResolutionBreached(Instant now) {
        Instant resolved = resolvedAt != null ? resolvedAt : closedAt;
        if (resolved != null) {
            return resolved.isAfter(resolutionDeadline());
        }
        return now != null && now.isAfter(resolutionDeadline());
    }

    public Instant responseDeadline() {
        return createdAt.plus(Duration.ofHours(slaResponseHours));
    }

    public Instant resolutionDeadline() {
        return createdAt.plus(Duration.ofHours(slaResolutionHours));
    }

    public List<TicketAttachment> activeAttachments() {
        return attachments.stream().filter(attachment -> !attachment.isRetired()).toList();
    }

    public TicketAttachment requireActiveAttachment(Long attachmentId) {
        if (attachmentId == null) {
            throw new DomainException("El adjunto es obligatorio");
        }
        return activeAttachments().stream()
                .filter(attachment -> attachmentId.equals(attachment.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Adjunto no encontrado"));
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getReporterUserId() {
        return reporterUserId;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public Long getSlaId() {
        return slaId;
    }

    public int getSlaResponseHours() {
        return slaResponseHours;
    }

    public int getSlaResolutionHours() {
        return slaResolutionHours;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFirstResponseAt() {
        return firstResponseAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public List<TicketAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    private void applySlaSnapshot(HelpdeskSla sla) {
        if (sla == null) {
            this.slaId = null;
            this.slaResponseHours = HelpdeskPolicy.DEFAULT_RESPONSE_HOURS;
            this.slaResolutionHours = HelpdeskPolicy.DEFAULT_RESOLUTION_HOURS;
            return;
        }
        if (sla.isRetired()) {
            throw new DomainException("El SLA está dado de baja");
        }
        this.slaId = sla.getId();
        this.slaResponseHours = sla.getResponseHours();
        this.slaResolutionHours = sla.getResolutionHours();
    }

    private void markFirstResponse(Instant when) {
        if (firstResponseAt == null) {
            this.firstResponseAt = when;
        }
    }

    private void assertOpen() {
        if (!status.isOpen()) {
            throw new DomainException("El ticket no admite cambios en estado " + status.name());
        }
    }

    private void assertNotCancelled() {
        if (status == TicketStatus.CANCELLED) {
            throw new DomainException("Un ticket anulado no admite adjuntos");
        }
    }

    private void applySubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new DomainException("El asunto del ticket es obligatorio");
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
        if (!(other instanceof HelpdeskTicket ticket)) {
            return false;
        }
        return id != null && id.equals(ticket.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
