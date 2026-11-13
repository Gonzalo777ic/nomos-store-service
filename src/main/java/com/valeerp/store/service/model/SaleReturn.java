package com.valeerp.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RMA / devolución. Libro con reverso; no delete físico de filas.
 */
@Entity
@Table(name = "sale_returns")
public class SaleReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnore
    private Sale sale;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleReturnType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleReturnStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "total_refund_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRefundAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_document_id")
    @JsonIgnoreProperties({"sale", "documents", "hibernateLazyInitializer", "handler"})
    private SalesDocument creditNote;

    @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleReturnDetail> details = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdByUserId;

    protected SaleReturn() {
    }

    public static SaleReturn draft(Sale sale, SaleReturnType type, String reason, String currency, LocalDateTime when) {
        if (sale == null) {
            throw new DomainException("La devolución exige una venta origen");
        }
        if (type == null) {
            throw new DomainException("El tipo de devolución es obligatorio");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainException("La moneda es obligatoria");
        }
        SaleReturn saleReturn = new SaleReturn();
        saleReturn.sale = sale;
        saleReturn.returnDate = when;
        saleReturn.type = type;
        saleReturn.status = SaleReturnStatus.DRAFT;
        saleReturn.reason = reason;
        saleReturn.currency = currency.trim().toUpperCase();
        saleReturn.totalRefundAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        return saleReturn;
    }

    public void addDetail(SaleDetail original, int quantity, Money unitNet) {
        assertDraft();
        if (original == null) {
            throw new DomainException("El detalle original es obligatorio");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad a devolver debe ser mayor que cero");
        }
        if (quantity > original.getQuantity()) {
            throw new DomainException("No se puede devolver más cantidad de la vendida");
        }
        if (unitNet == null) {
            throw new DomainException("El importe unitario de reverso es obligatorio");
        }
        if (!currency.equals(unitNet.currency())) {
            throw new DomainException("La moneda del reverso no coincide");
        }
        Money lineTotal = unitNet.times(quantity);
        SaleReturnDetail detail = SaleReturnDetail.create(this, original, quantity, unitNet, lineTotal);
        details.add(detail);
        recalculateRefund();
    }

    public void confirm(SalesDocument creditNote) {
        assertDraft();
        if (details.isEmpty()) {
            throw new DomainException("La devolución no tiene líneas");
        }
        if (creditNote == null) {
            throw new DomainException("La nota de crédito es obligatoria al confirmar");
        }
        this.creditNote = creditNote;
        this.status = SaleReturnStatus.CONFIRMED;
    }

    /**
     * Anula una devolución confirmada dejando el libro intacto (estado CANCELLED + NC void).
     */
    public void reverse() {
        if (status != SaleReturnStatus.CONFIRMED) {
            throw new DomainException("Solo una devolución confirmada admite reverso");
        }
        this.status = SaleReturnStatus.CANCELLED;
        if (creditNote != null) {
            creditNote.voidDocument("Reverso de devolución " + id);
        }
    }

    public void cancelDraft() {
        assertDraft();
        this.status = SaleReturnStatus.CANCELLED;
    }

    public Money refundMoney() {
        return Money.of(totalRefundAmount, currency);
    }

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public SaleReturnType getType() {
        return type;
    }

    public SaleReturnStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public SalesDocument getCreditNote() {
        return creditNote;
    }

    public List<SaleReturnDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    private void recalculateRefund() {
        Money total = Money.zero(currency);
        for (SaleReturnDetail detail : details) {
            total = total.plus(detail.subtotalMoney());
        }
        this.totalRefundAmount = total.amount();
        if (type == SaleReturnType.TOTAL && sale.getTotalAmount() != null
                && total.amount().compareTo(sale.getTotalAmount()) > 0) {
            throw new DomainException("El reembolso total no puede superar la venta");
        }
    }

    private void assertDraft() {
        if (status != SaleReturnStatus.DRAFT) {
            throw new DomainException("La devolución ya no admite cambios (" + status + ")");
        }
    }

}
