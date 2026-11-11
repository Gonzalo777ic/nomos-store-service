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
 * Condición de pago de la ficha comercial Partner (nunca en auth User).
 */
@Entity
@Table(name = "payment_terms")
public class PaymentTerm extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "days_due", nullable = false)
    private int daysDue;

    @Column(length = 240)
    private String description;

    protected PaymentTerm() {
    }

    public static PaymentTerm create(String code, String name, int daysDue, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de condición de pago es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de condición de pago es obligatorio");
        }
        if (daysDue < 0) {
            throw new DomainException("Los días de crédito no pueden ser negativos");
        }
        PaymentTerm term = new PaymentTerm();
        term.code = code.trim().toUpperCase();
        term.name = name.trim();
        term.daysDue = daysDue;
        term.description = blankToNull(description);
        return term;
    }

    public void rename(String name, int daysDue, String description) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de condición de pago es obligatorio");
        }
        if (daysDue < 0) {
            throw new DomainException("Los días de crédito no pueden ser negativos");
        }
        this.name = name.trim();
        this.daysDue = daysDue;
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

    public int getDaysDue() {
        return daysDue;
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
        if (!(other instanceof PaymentTerm term)) {
            return false;
        }
        return id != null && id.equals(term.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
