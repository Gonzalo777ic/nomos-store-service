package com.valeerp.crm;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
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
 * Lead comercial. Maestro con soft-delete. Separado del User de identidad; puede enlazar Partner por id.
 */
@Entity
@Table(name = "crm_leads")
public class CrmLead extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(length = 120)
    private String email;

    @Column(length = 60)
    private String phone;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(length = 80)
    private String source;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private LeadStatus status;

    @Column(name = "disqualify_reason", length = 400)
    private String disqualifyReason;

    @Column(name = "converted_opportunity_id")
    private Long convertedOpportunityId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CrmLead() {
    }

    public static CrmLead capture(String title, String contactName, String email, String phone,
                                  Long partnerId, String source, String notes, Instant createdAt) {
        CrmLead lead = new CrmLead();
        lead.applyProfile(title, contactName, email, phone, partnerId, source, notes);
        lead.status = LeadStatus.OPEN;
        lead.createdAt = createdAt;
        return lead;
    }

    public void updateProfile(String title, String contactName, String email, String phone,
                              Long partnerId, String source, String notes) {
        assertEditable();
        applyProfile(title, contactName, email, phone, partnerId, source, notes);
    }

    public void qualify() {
        assertEditable();
        if (status == LeadStatus.DISQUALIFIED) {
            throw new DomainException("Un lead descalificado no se puede calificar");
        }
        this.status = LeadStatus.QUALIFIED;
    }

    public void disqualify(String reason) {
        assertEditable();
        if (reason == null || reason.isBlank()) {
            throw new DomainException("El motivo de descalificación es obligatorio");
        }
        this.status = LeadStatus.DISQUALIFIED;
        this.disqualifyReason = reason.trim();
    }

    public void markConverted(Long opportunityId) {
        assertEditable();
        if (opportunityId == null) {
            throw new DomainException("La oportunidad convertida es obligatoria");
        }
        this.status = LeadStatus.CONVERTED;
        this.convertedOpportunityId = opportunityId;
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public String getSource() {
        return source;
    }

    public String getNotes() {
        return notes;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public String getDisqualifyReason() {
        return disqualifyReason;
    }

    public Long getConvertedOpportunityId() {
        return convertedOpportunityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void assertEditable() {
        if (status == LeadStatus.CONVERTED) {
            throw new DomainException("El lead ya fue convertido a oportunidad");
        }
        if (isRetired()) {
            throw new DomainException("El lead está dado de baja");
        }
    }

    private void applyProfile(String title, String contactName, String email, String phone,
                              Long partnerId, String source, String notes) {
        if (title == null || title.isBlank()) {
            throw new DomainException("El título del lead es obligatorio");
        }
        this.title = title.trim();
        this.contactName = blankToNull(contactName);
        this.email = blankToNull(email);
        this.phone = blankToNull(phone);
        this.partnerId = partnerId;
        this.source = blankToNull(source);
        this.notes = blankToNull(notes);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CrmLead lead)) {
            return false;
        }
        return id != null && id.equals(lead.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
