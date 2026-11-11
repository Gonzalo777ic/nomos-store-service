package com.valeerp.partner;

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

@Entity
@Table(name = "partner_tags")
public class PartnerTag extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(nullable = false, length = 80)
    private String label;

    protected PartnerTag() {
    }

    static PartnerTag create(Partner partner, String label) {
        if (label == null || label.isBlank()) {
            throw new DomainException("La etiqueta no puede estar vacía");
        }
        PartnerTag tag = new PartnerTag();
        tag.partner = partner;
        tag.label = label.trim();
        return tag;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    boolean sameLabel(String other) {
        return label.equalsIgnoreCase(other == null ? "" : other.trim());
    }
}
