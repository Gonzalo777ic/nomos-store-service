package com.valeerp.treasury;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;

/**
 * Caja registradora (maestro). Soft-delete; no borra histórico de sesiones ni movimientos.
 */
@Entity
@Table(name = "cash_registers")
public class CashRegister extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = TreasuryPolicy.CODE_MAX_LENGTH)
    private String code;

    @Column(nullable = false, length = TreasuryPolicy.NAME_MAX_LENGTH)
    private String name;

    @Column(name = "journal_id")
    private Long journalId;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CashRegister() {
    }

    public static CashRegister open(Long companyId, String code, String name, Long journalId,
                                    String currencyCode, Instant createdAt) {
        CashRegister register = new CashRegister();
        register.companyId = requireCompanyId(companyId);
        register.applyIdentity(code, name, journalId, currencyCode);
        register.createdAt = createdAt == null ? Instant.now() : createdAt;
        return register;
    }

    public void rename(String code, String name, Long journalId, String currencyCode) {
        applyIdentity(code, name, journalId, currencyCode);
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Long getJournalId() {
        return journalId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    static String normalizeCode(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    private void applyIdentity(String rawCode, String rawName, Long journalId, String currencyCode) {
        String normalized = normalizeCode(rawCode);
        if (normalized.isEmpty()) {
            throw new DomainException("El código de caja es obligatorio");
        }
        if (normalized.length() > TreasuryPolicy.CODE_MAX_LENGTH) {
            throw new DomainException("El código de caja excede la longitud permitida");
        }
        if (rawName == null || rawName.isBlank()) {
            throw new DomainException("El nombre de caja es obligatorio");
        }
        String trimmedName = rawName.trim();
        if (trimmedName.length() > TreasuryPolicy.NAME_MAX_LENGTH) {
            throw new DomainException("El nombre de caja excede la longitud permitida");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new DomainException("La moneda de la caja es obligatoria");
        }
        this.code = normalized;
        this.name = trimmedName;
        this.journalId = journalId;
        this.currencyCode = currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    private static Long requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        return companyId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CashRegister register)) {
            return false;
        }
        return id != null && id.equals(register.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
