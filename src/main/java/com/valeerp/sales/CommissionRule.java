package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Regla simple de comisión por porcentaje. Maestro con soft-delete.
 */
@Entity
@Table(name = "commission_rules")
public class CommissionRule extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    /** Fracción decimal (0.05 = 5%). Nunca se hardcodea en el controlador. */
    @Column(name = "percent_rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal percentRate;

    @Column(length = 240)
    private String description;

    protected CommissionRule() {
    }

    public static CommissionRule create(String code, String name, BigDecimal percentRate, String description) {
        CommissionRule rule = new CommissionRule();
        rule.rename(code, name, percentRate, description);
        return rule;
    }

    public void rename(String code, String name, BigDecimal percentRate, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de la regla de comisión es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la regla de comisión es obligatorio");
        }
        if (percentRate == null || percentRate.signum() < 0) {
            throw new DomainException("El porcentaje de comisión no es válido");
        }
        if (percentRate.compareTo(BigDecimal.ONE) > 0) {
            // permite enviar 5 para 5% o 0.05
            percentRate = percentRate.movePointLeft(2);
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.percentRate = percentRate;
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

    public BigDecimal getPercentRate() {
        return percentRate;
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
        if (!(other instanceof CommissionRule rule)) {
            return false;
        }
        return id != null && id.equals(rule.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
