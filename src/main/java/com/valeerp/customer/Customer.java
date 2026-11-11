package com.valeerp.customer;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Proyección ecommerce de cliente (piloto del kernel SoftDeletable + Actor + reloj).
 * La ficha comercial canónica vive en {@code com.valeerp.partner.Partner}.
 */
@Entity
@Table(name = "customers")
public class Customer extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth0_id", unique = true)
    private String auth0Id;

    @Column(nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    private String phone;
    private String address;

    public Long getId() {
        return id;
    }

    public String getAuth0Id() {
        return auth0Id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void register(String auth0Id, String email, String fullName, String documentType,
                         String documentNumber, String phone, String address) {
        this.auth0Id = auth0Id;
        this.email = email;
        this.fullName = fullName == null || fullName.isBlank() ? email : fullName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.phone = phone;
        this.address = address;
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
    }
}
