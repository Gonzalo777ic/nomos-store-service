package com.valeerp.accounting;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
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
import java.util.Objects;

/**
 * Diario contable configurable por compañía (ventas, compras, banco, caja, misceláneo).
 * {@code companyId} es referencia local; sin Feign a auth. Maestro con soft-delete.
 */
@Entity
@Table(name = "accounting_journals")
public class Journal extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = AccountingPolicy.JOURNAL_CODE_MAX_LENGTH)
    private String code;

    @Column(nullable = false, length = AccountingPolicy.JOURNAL_NAME_MAX_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private JournalKind kind;

    @Column(name = "default_account_id")
    private Long defaultAccountId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Journal() {
    }

    public static Journal open(Long companyId, String code, String name, JournalKind kind, Instant createdAt) {
        return open(companyId, code, name, kind, null, createdAt);
    }

    public static Journal open(Long companyId, String code, String name, JournalKind kind,
                               Long defaultAccountId, Instant createdAt) {
        Journal journal = new Journal();
        journal.companyId = requireCompanyId(companyId);
        journal.applyIdentity(code, name, kind, defaultAccountId);
        journal.createdAt = createdAt == null ? Instant.now() : createdAt;
        return journal;
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de diario es obligatorio");
        }
        String trimmed = name.trim();
        if (trimmed.length() > AccountingPolicy.JOURNAL_NAME_MAX_LENGTH) {
            throw new DomainException("El nombre de diario excede la longitud permitida");
        }
        this.name = trimmed;
    }

    public void reconfigure(String code, String name, JournalKind kind, Long defaultAccountId) {
        applyIdentity(code, name, kind, defaultAccountId);
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

    public JournalKind getKind() {
        return kind;
    }

    public Long getDefaultAccountId() {
        return defaultAccountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void applyIdentity(String rawCode, String rawName, JournalKind kind, Long defaultAccountId) {
        if (kind == null) {
            throw new DomainException("El tipo de diario es obligatorio");
        }
        String normalizedCode = normalizeCode(rawCode);
        if (normalizedCode.isEmpty()) {
            throw new DomainException("El código de diario es obligatorio");
        }
        if (normalizedCode.length() > AccountingPolicy.JOURNAL_CODE_MAX_LENGTH) {
            throw new DomainException("El código de diario excede la longitud permitida");
        }
        if (rawName == null || rawName.isBlank()) {
            throw new DomainException("El nombre de diario es obligatorio");
        }
        String trimmedName = rawName.trim();
        if (trimmedName.length() > AccountingPolicy.JOURNAL_NAME_MAX_LENGTH) {
            throw new DomainException("El nombre de diario excede la longitud permitida");
        }
        this.code = normalizedCode;
        this.name = trimmedName;
        this.kind = kind;
        this.defaultAccountId = defaultAccountId;
    }

    private static Long requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        return companyId;
    }

    static String normalizeCode(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Journal journal) || id == null) {
            return false;
        }
        return id.equals(journal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
