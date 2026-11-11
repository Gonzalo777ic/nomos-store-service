package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_return_details")
public class SaleReturnDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_return_id", nullable = false)
    @JsonIgnore
    private SaleReturn saleReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_sale_detail_id", nullable = false)
    @JsonIgnoreProperties({"sale", "hibernateLazyInitializer", "handler"})
    private SaleDetail originalSaleDetail;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    protected SaleReturnDetail() {
    }

    static SaleReturnDetail create(SaleReturn saleReturn, SaleDetail original, int quantity,
                                   Money unitPrice, Money lineTotal) {
        if (saleReturn == null || original == null) {
            throw new DomainException("La línea de devolución exige origen");
        }
        SaleReturnDetail detail = new SaleReturnDetail();
        detail.saleReturn = saleReturn;
        detail.originalSaleDetail = original;
        detail.quantity = quantity;
        detail.unitPrice = unitPrice.amount();
        detail.subtotal = lineTotal.amount();
        return detail;
    }

    Money subtotalMoney() {
        return Money.of(subtotal, saleReturn.getCurrency());
    }

    public Long getId() {
        return id;
    }

    public SaleReturn getSaleReturn() {
        return saleReturn;
    }

    public SaleDetail getOriginalSaleDetail() {
        return originalSaleDetail;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
