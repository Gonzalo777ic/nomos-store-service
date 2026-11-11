package com.valeerp.partner;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
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

@Entity
@Table(name = "partner_addresses")
public class PartnerAddress extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 32)
    private AddressKind kind;

    @Column(nullable = false, length = 240)
    private String line;

    @Column(length = 120)
    private String city;

    @Column(length = 120)
    private String region;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(length = 120)
    private String country;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryAddress;

    protected PartnerAddress() {
    }

    static PartnerAddress create(Partner partner, AddressKind kind, String line, String city,
                                 String region, String postalCode, String country, boolean primary) {
        if (line == null || line.isBlank()) {
            throw new DomainException("La dirección exige una línea");
        }
        PartnerAddress address = new PartnerAddress();
        address.partner = partner;
        address.kind = kind == null ? AddressKind.OTHER : kind;
        address.line = line.trim();
        address.city = blankToNull(city);
        address.region = blankToNull(region);
        address.postalCode = blankToNull(postalCode);
        address.country = blankToNull(country);
        address.primaryAddress = primary;
        return address;
    }

    void markPrimary(boolean primary) {
        this.primaryAddress = primary;
    }

    public Long getId() {
        return id;
    }

    public AddressKind getKind() {
        return kind;
    }

    public String getLine() {
        return line;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public boolean isPrimaryAddress() {
        return primaryAddress;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
