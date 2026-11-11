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
@Table(name = "partner_contacts")
public class PartnerContact extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(nullable = false, length = 180)
    private String fullName;

    @Column(length = 120)
    private String email;

    @Column(length = 60)
    private String phone;

    @Column(length = 120)
    private String roleLabel;

    protected PartnerContact() {
    }

    static PartnerContact create(Partner partner, String fullName, String email, String phone, String roleLabel) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("El contacto hijo exige un nombre");
        }
        PartnerContact contact = new PartnerContact();
        contact.partner = partner;
        contact.fullName = fullName.trim();
        contact.email = blankToNull(email);
        contact.phone = blankToNull(phone);
        contact.roleLabel = blankToNull(roleLabel);
        return contact;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
