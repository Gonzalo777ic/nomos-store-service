package com.nomos.store.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import com.valeerp.sales.TaxKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Catálogo fiscal local persistido (#73). La tasa nunca se hardcodea (p. ej. 1.18) en venta/compra.
 */
@Entity
@Table(name = "tax_rates")
public class TaxRate extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_kind", nullable = false, length = 32)
    private TaxKind kind = TaxKind.IGV;

    /** Fracción decimal (p. ej. 0.18). Nunca el factor 1.18. */
    @Column(name = "rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal rate;

    protected TaxRate() {
    }

    public static TaxRate create(String name, TaxKind kind, BigDecimal rate) {
        TaxRate taxRate = new TaxRate();
        taxRate.rename(name);
        taxRate.changeKind(kind);
        taxRate.changeRate(rate);
        return taxRate;
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la tasa es obligatorio");
        }
        this.name = name.trim();
    }

    public void changeKind(TaxKind kind) {
        if (kind == null) {
            throw new DomainException("El tipo de impuesto es obligatorio");
        }
        this.kind = kind;
    }

    public void changeRate(BigDecimal rate) {
        if (rate == null || rate.signum() < 0) {
            throw new DomainException("La tasa de impuesto no es válida");
        }
        TaxKind effectiveKind = getKind();
        if (effectiveKind == TaxKind.EXEMPT || effectiveKind == TaxKind.UNAFFECTED) {
            if (rate.signum() != 0) {
                throw new DomainException("Un impuesto exonerado/inafecto exige tasa cero");
            }
        }
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TaxKind getKind() {
        return kind == null ? TaxKind.IGV : kind;
    }

    public BigDecimal getRate() {
        return rate;
    }

    /** Alias semántico para Money.includedTax / prorrateo. */
    public BigDecimal rateAsDecimal() {
        return rate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TaxRate taxRate)) {
            return false;
        }
        return id != null && id.equals(taxRate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
