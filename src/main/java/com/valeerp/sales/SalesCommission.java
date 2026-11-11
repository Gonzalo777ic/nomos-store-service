package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
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
 * Comisión de vendedor calculada en backend. Libro: cancelación por estado, no delete.
 */
@Entity
@Table(name = "sales_commissions")
public class SalesCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_id")
    private Long saleId;

    @Column(name = "sale_order_id")
    private Long saleOrderId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "commission_rule_id", nullable = false)
    private Long commissionRuleId;

    @Column(name = "rule_code", nullable = false, length = 40)
    private String ruleCode;

    @Column(name = "percent_rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal percentRate;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "commission_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SalesCommissionStatus status;

    @Column(name = "accrued_at", nullable = false)
    private Instant accruedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected SalesCommission() {
    }

    public static SalesCommission accrue(Long saleId, Long saleOrderId, Long sellerId,
                                         CommissionRule rule, Money base, Instant when) {
        if (sellerId == null) {
            throw new DomainException("La comisión exige un vendedor");
        }
        if (rule == null) {
            throw new DomainException("La comisión exige una regla");
        }
        if (base == null) {
            throw new DomainException("La base de comisión es obligatoria");
        }
        if (saleId == null && saleOrderId == null) {
            throw new DomainException("La comisión exige saleId o saleOrderId");
        }
        Money amount = base.times(rule.getPercentRate());
        SalesCommission commission = new SalesCommission();
        commission.saleId = saleId;
        commission.saleOrderId = saleOrderId;
        commission.sellerId = sellerId;
        commission.commissionRuleId = rule.getId();
        commission.ruleCode = rule.getCode();
        commission.percentRate = rule.getPercentRate();
        commission.baseAmount = base.amount();
        commission.commissionAmount = amount.amount();
        commission.currency = base.currency();
        commission.status = SalesCommissionStatus.ACCRUED;
        commission.accruedAt = when;
        return commission;
    }

    public void cancel(Instant when) {
        if (status == SalesCommissionStatus.CANCELLED) {
            throw new DomainException("La comisión ya está cancelada");
        }
        this.status = SalesCommissionStatus.CANCELLED;
        this.cancelledAt = when;
    }

    public Money commissionMoney() {
        return Money.of(commissionAmount, currency);
    }

    public Long getId() {
        return id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public Long getSaleOrderId() {
        return saleOrderId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getCommissionRuleId() {
        return commissionRuleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public BigDecimal getPercentRate() {
        return percentRate;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public SalesCommissionStatus getStatus() {
        return status;
    }

    public Instant getAccruedAt() {
        return accruedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SalesCommission commission)) {
            return false;
        }
        return id != null && id.equals(commission.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
