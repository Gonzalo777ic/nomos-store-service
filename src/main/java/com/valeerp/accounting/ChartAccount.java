package com.valeerp.accounting;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Cuenta del plan jerárquico (PCGE-like). Maestro con soft-delete.
 * {@code companyId} es referencia local; no hay Feign a Identidad.
 */
@Entity
@Table(name = "chart_accounts")
public class ChartAccount extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = AccountingPolicy.ACCOUNT_CODE_MAX_LENGTH)
    private String code;

    @Column(nullable = false, length = AccountingPolicy.ACCOUNT_NAME_MAX_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AccountClass accountClass;

    @Column(name = "allows_posting", nullable = false)
    private boolean allowsPosting;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ChartAccount parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ChartAccount> children = new ArrayList<>();

    protected ChartAccount() {
    }

    public static ChartAccount open(Long companyId, String code, String name, AccountClass accountClass,
                                    boolean allowsPosting, ChartAccount parent, Instant createdAt) {
        ChartAccount account = new ChartAccount();
        account.companyId = requireCompanyId(companyId);
        account.applyIdentity(code, name, accountClass, allowsPosting);
        account.createdAt = createdAt == null ? Instant.now() : createdAt;
        if (parent != null) {
            account.attachTo(parent);
        }
        return account;
    }

    public void rename(String code, String name, AccountClass accountClass, boolean allowsPosting) {
        applyIdentity(code, name, accountClass, allowsPosting);
    }

    public void attachTo(ChartAccount newParent) {
        if (newParent == null) {
            this.parent = null;
            return;
        }
        if (newParent.isRetired()) {
            throw new DomainException("La cuenta padre está dada de baja");
        }
        if (!this.companyId.equals(newParent.companyId)) {
            throw new DomainException("La cuenta padre debe pertenecer a la misma compañía");
        }
        if (this.equals(newParent) || isAncestorOf(newParent)) {
            throw new DomainException("La jerarquía de cuentas no puede formar un ciclo");
        }
        if (newParent.allowsPosting) {
            throw new DomainException("Una cuenta imputable no puede ser padre");
        }
        this.parent = newParent;
        if (!newParent.children.contains(this)) {
            newParent.children.add(this);
        }
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
        for (ChartAccount child : children) {
            if (!child.isRetired()) {
                child.archive(actor, when);
            }
        }
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

    public AccountClass getAccountClass() {
        return accountClass;
    }

    public boolean isAllowsPosting() {
        return allowsPosting;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ChartAccount getParent() {
        return parent;
    }

    public Long getParentId() {
        return parent == null ? null : parent.getId();
    }

    public List<ChartAccount> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<ChartAccount> activeChildren() {
        return children.stream().filter(child -> !child.isRetired()).toList();
    }

    public boolean sameCode(String otherCode) {
        return code.equals(normalizeCode(otherCode));
    }

    private void applyIdentity(String rawCode, String rawName, AccountClass accountClass, boolean allowsPosting) {
        if (accountClass == null) {
            throw new DomainException("La clase de cuenta es obligatoria");
        }
        String normalizedCode = normalizeCode(rawCode);
        if (normalizedCode.isEmpty()) {
            throw new DomainException("El código de cuenta es obligatorio");
        }
        if (normalizedCode.length() > AccountingPolicy.ACCOUNT_CODE_MAX_LENGTH) {
            throw new DomainException("El código de cuenta excede la longitud permitida");
        }
        if (rawName == null || rawName.isBlank()) {
            throw new DomainException("El nombre de cuenta es obligatorio");
        }
        String trimmedName = rawName.trim();
        if (trimmedName.length() > AccountingPolicy.ACCOUNT_NAME_MAX_LENGTH) {
            throw new DomainException("El nombre de cuenta excede la longitud permitida");
        }
        if (allowsPosting && !activeChildren().isEmpty()) {
            throw new DomainException("Una cuenta con hijos no puede ser imputable");
        }
        this.code = normalizedCode;
        this.name = trimmedName;
        this.accountClass = accountClass;
        this.allowsPosting = allowsPosting;
    }

    private boolean isAncestorOf(ChartAccount other) {
        ChartAccount cursor = other.parent;
        while (cursor != null) {
            if (this.equals(cursor)) {
                return true;
            }
            cursor = cursor.parent;
        }
        return false;
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
        if (!(other instanceof ChartAccount account)) {
            return false;
        }
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
