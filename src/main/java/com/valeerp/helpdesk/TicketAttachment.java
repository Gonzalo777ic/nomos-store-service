package com.valeerp.helpdesk;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Adjunto de ticket como metadatos locales (sin storage externo obligatorio). Soft-delete.
 */
@Entity
@Table(name = "helpdesk_ticket_attachments")
public class TicketAttachment extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private HelpdeskTicket ticket;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TicketAttachment() {
    }

    static TicketAttachment attach(HelpdeskTicket ticket, String label, String url, String contentType,
                                   Long sizeBytes, String fileName, Instant createdAt) {
        if (ticket == null) {
            throw new DomainException("El adjunto exige un ticket");
        }
        TicketAttachment attachment = new TicketAttachment();
        attachment.ticket = ticket;
        attachment.applyLabel(label);
        attachment.applyUrl(url);
        attachment.contentType = blankToNull(contentType);
        attachment.applySize(sizeBytes);
        attachment.fileName = blankToNull(fileName);
        attachment.createdAt = createdAt;
        return attachment;
    }

    void relabel(String label) {
        applyLabel(label);
    }

    void archive(Actor actor, Instant when) {
        retire(actor, when);
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticket == null ? null : ticket.getId();
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void applyLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new DomainException("La etiqueta del adjunto es obligatoria");
        }
        this.label = label.trim();
    }

    private void applyUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new DomainException("La URL del adjunto es obligatoria");
        }
        this.url = url.trim();
    }

    private void applySize(Long sizeBytes) {
        if (sizeBytes != null && sizeBytes < 0) {
            throw new DomainException("El tamaño del adjunto no puede ser negativo");
        }
        this.sizeBytes = sizeBytes;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TicketAttachment attachment)) {
            return false;
        }
        return id != null && id.equals(attachment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
