package com.valeerp.partner;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Categoría comercial de partner (maestro con soft-delete).
 */
@Entity
@Table(name = "partner_categories")
public class PartnerCategory extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 240)
    private String description;

    protected PartnerCategory() {
    }

    public static PartnerCategory create(String code, String name, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de categoría es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de categoría es obligatorio");
        }
        PartnerCategory category = new PartnerCategory();
        category.code = code.trim().toUpperCase();
        category.name = name.trim();
        category.description = blankToNull(description);
        return category;
    }

    public void rename(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de categoría es obligatorio");
        }
        this.name = name.trim();
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
        if (!(other instanceof PartnerCategory category)) {
            return false;
        }
        return id != null && id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
