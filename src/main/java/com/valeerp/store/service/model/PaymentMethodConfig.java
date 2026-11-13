package com.valeerp.store.service.model;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import com.valeerp.treasury.PaymentMethodKind;
import com.valeerp.treasury.TreasuryPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;

/**
 * Método de pago configurable y persistido (maestro con soft-delete).
 */
@Entity
@Table(name = "payment_method_configs")
public class PaymentMethodConfig extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = TreasuryPolicy.CODE_MAX_LENGTH)
    private String code;

    @Column(name = "name", nullable = false, length = TreasuryPolicy.NAME_MAX_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private PaymentMethodKind kind;

    @Column(name = "requires_reference", nullable = false)
    private boolean requiresReference;

    protected PaymentMethodConfig() {
    }

    public static PaymentMethodConfig create(String code, String name, PaymentMethodKind kind,
                                             boolean requiresReference) {
        PaymentMethodConfig method = new PaymentMethodConfig();
        method.applyIdentity(code, name, kind, requiresReference);
        return method;
    }

    public void rename(String code, String name, PaymentMethodKind kind, boolean requiresReference) {
        applyIdentity(code, name, kind, requiresReference);
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
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

    public PaymentMethodKind getKind() {
        return kind;
    }

    /** Compatibilidad con lecturas legacy que esperaban {@code type} textual. */
    public String getType() {
        return kind == null ? null : kind.name();
    }

    public boolean isRequiresReference() {
        return requiresReference;
    }

    public static String normalizeCode(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    private void applyIdentity(String rawCode, String rawName, PaymentMethodKind kind, boolean requiresReference) {
        String normalized = normalizeCode(rawCode);
        if (normalized.isEmpty()) {
            throw new DomainException("El código de método de pago es obligatorio");
        }
        if (normalized.length() > TreasuryPolicy.CODE_MAX_LENGTH) {
            throw new DomainException("El código de método de pago excede la longitud permitida");
        }
        if (rawName == null || rawName.isBlank()) {
            throw new DomainException("El nombre de método de pago es obligatorio");
        }
        if (kind == null) {
            throw new DomainException("El tipo de método de pago es obligatorio");
        }
        this.code = normalized;
        this.name = rawName.trim();
        this.kind = kind;
        this.requiresReference = requiresReference;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PaymentMethodConfig method)) {
            return false;
        }
        return id != null && id.equals(method.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
