package com.valeerp.sales;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Maestro de promoción (% / monto / 2x1). Soft-delete; la aplicación de descuento ocurre en SaleOrder.
 */
@Entity
@Table(name = "catalog_promotions")
public class CatalogPromotion extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unicidad vigente se valida en servicio (índice parcial deleted_at IS NULL en migración). */
    @Column(nullable = false, length = 120)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PromotionKind kind;

    /** Porcentaje (p.ej. 10) o monto unitario según {@link #kind}. Ignorado en TWO_FOR_ONE. */
    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountValue;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CatalogPromotion() {
    }

    public static CatalogPromotion define(String code, String name, PromotionKind kind,
                                          BigDecimal discountValue, String currency,
                                          Instant startsAt, Instant endsAt, Instant createdAt) {
        CatalogPromotion promotion = new CatalogPromotion();
        promotion.applyIdentity(code, name);
        promotion.applyRule(kind, discountValue, currency);
        promotion.applyWindow(startsAt, endsAt);
        promotion.createdAt = createdAt;
        return promotion;
    }

    public void rename(String code, String name) {
        applyIdentity(code, name);
    }

    public void changeRule(PromotionKind kind, BigDecimal discountValue, String currency) {
        applyRule(kind, discountValue, currency);
    }

    public void changeWindow(Instant startsAt, Instant endsAt) {
        applyWindow(startsAt, endsAt);
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
    }

    public boolean isActiveAt(Instant when) {
        if (isRetired() || when == null) {
            return false;
        }
        return !when.isBefore(startsAt) && !when.isAfter(endsAt);
    }

    public PromotionSpec toSpec() {
        return new PromotionSpec(id, kind, discountValue, currency);
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

    public PromotionKind getKind() {
        return kind;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void applyIdentity(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de promoción es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de promoción es obligatorio");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
    }

    private void applyRule(PromotionKind kind, BigDecimal discountValue, String currency) {
        if (kind == null) {
            throw new DomainException("El tipo de promoción es obligatorio");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainException("La moneda de la promoción es obligatoria");
        }
        BigDecimal value = discountValue == null ? BigDecimal.ZERO : discountValue;
        if (kind == PromotionKind.TWO_FOR_ONE) {
            this.discountValue = BigDecimal.ZERO;
        } else {
            if (value.signum() < 0) {
                throw new DomainException("El valor de descuento no puede ser negativo");
            }
            if (kind == PromotionKind.PERCENT && value.compareTo(new BigDecimal("100")) > 0) {
                throw new DomainException("El porcentaje de promoción no puede superar 100");
            }
            if (kind == PromotionKind.AMOUNT) {
                Money.of(value, currency.trim().toUpperCase());
            }
            this.discountValue = value;
        }
        this.kind = kind;
        this.currency = currency.trim().toUpperCase();
    }

    private void applyWindow(Instant startsAt, Instant endsAt) {
        if (startsAt == null || endsAt == null) {
            throw new DomainException("La vigencia de la promoción es obligatoria");
        }
        if (endsAt.isBefore(startsAt)) {
            throw new DomainException("La fecha fin de promoción no puede ser anterior al inicio");
        }
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CatalogPromotion promotion)) {
            return false;
        }
        return id != null && id.equals(promotion.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
