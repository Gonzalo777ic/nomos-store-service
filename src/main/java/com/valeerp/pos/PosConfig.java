package com.valeerp.pos;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Configuración POS por compañía: caja, diario y lista de precios por id local.
 * Maestro con soft-delete. No acopla entidades de tesorería/contabilidad (refs por id).
 */
@Entity
@Table(name = "pos_configs")
public class PosConfig extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    /** Id local de caja (tesorería). */
    @Column(name = "cash_register_id", nullable = false)
    private Long cashRegisterId;

    /** Id local de diario contable. */
    @Column(name = "journal_id", nullable = false)
    private Long journalId;

    /** Id local de lista de precios (proyección). */
    @Column(name = "price_list_id", nullable = false)
    private Long priceListId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PosConfig() {
    }

    public static PosConfig create(Long companyId, String code, String name,
                                   Long cashRegisterId, Long journalId, Long priceListId,
                                   String currencyCode, Instant createdAt) {
        PosConfig config = new PosConfig();
        config.companyId = requirePositive(companyId, "companyId");
        config.applyCode(code);
        config.applyName(name);
        config.cashRegisterId = requirePositive(cashRegisterId, "cashRegisterId");
        config.journalId = requirePositive(journalId, "journalId");
        config.priceListId = requirePositive(priceListId, "priceListId");
        config.currency = Currency.of(currencyCode).code();
        config.createdAt = createdAt == null ? Instant.now() : createdAt;
        return config;
    }

    public void rename(String name) {
        applyName(name);
    }

    public void rebind(Long cashRegisterId, Long journalId, Long priceListId) {
        this.cashRegisterId = requirePositive(cashRegisterId, "cashRegisterId");
        this.journalId = requirePositive(journalId, "journalId");
        this.priceListId = requirePositive(priceListId, "priceListId");
    }

    public void changeCurrency(String currencyCode) {
        this.currency = Currency.of(currencyCode).code();
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

    public Long getCashRegisterId() {
        return cashRegisterId;
    }

    public Long getJournalId() {
        return journalId;
    }

    public Long getPriceListId() {
        return priceListId;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void applyCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código POS es obligatorio");
        }
        this.code = code.trim().toUpperCase();
    }

    private void applyName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre POS es obligatorio");
        }
        this.name = name.trim();
    }

    private static Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new DomainException(field + " es obligatorio");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PosConfig config)) {
            return false;
        }
        return id != null && id.equals(config.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
