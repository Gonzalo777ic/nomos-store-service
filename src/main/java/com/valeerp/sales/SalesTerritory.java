package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Territorio comercial. Maestro con soft-delete.
 */
@Entity
@Table(name = "sales_territories")
public class SalesTerritory extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String region;

    @Column(length = 240)
    private String description;

    protected SalesTerritory() {
    }

    public static SalesTerritory create(String code, String name, String region, String description) {
        SalesTerritory territory = new SalesTerritory();
        territory.rename(code, name, region, description);
        return territory;
    }

    public void rename(String code, String name, String region, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código del territorio es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del territorio es obligatorio");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.region = blankToNull(region);
        this.description = blankToNull(description);
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

    public String getRegion() {
        return region;
    }

    public String getDescription() {
        return description;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SalesTerritory territory)) {
            return false;
        }
        return id != null && id.equals(territory.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
