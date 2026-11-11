package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Documento comercial del pedido confirmado. Separado de la factura fiscal (SalesDocument BOLETA/FACTURA).
 */
@Entity
@Table(name = "sale_order_documents")
public class SaleOrderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_order_id", nullable = false, unique = true)
    private SaleOrder saleOrder;

    @Column(nullable = false, length = 8)
    private String series;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "invoicing_policy", nullable = false, length = 24)
    private String invoicingPolicy;

    protected SaleOrderDocument() {
    }

    static SaleOrderDocument issue(SaleOrder order, String series, String number, Instant issuedAt) {
        if (order == null) {
            throw new DomainException("El documento comercial exige un pedido");
        }
        if (series == null || series.isBlank()) {
            throw new DomainException("La serie del documento comercial es obligatoria");
        }
        if (number == null || number.isBlank()) {
            throw new DomainException("El número del documento comercial es obligatorio");
        }
        if (issuedAt == null) {
            throw new DomainException("La fecha de emisión es obligatoria");
        }
        Money total = order.total();
        SaleOrderDocument document = new SaleOrderDocument();
        document.saleOrder = order;
        document.series = series.trim().toUpperCase();
        document.number = number.trim();
        document.issuedAt = issuedAt;
        document.totalAmount = total.amount();
        document.currency = total.currency();
        document.invoicingPolicy = order.getInvoicingPolicy().name();
        return document;
    }

    public String fullNumber() {
        return series + "-" + number;
    }

    public Money total() {
        return Money.of(totalAmount, currency);
    }

    public Long getId() {
        return id;
    }

    public Long getSaleOrderId() {
        return saleOrder == null ? null : saleOrder.getId();
    }

    public String getSeries() {
        return series;
    }

    public String getNumber() {
        return number;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getInvoicingPolicy() {
        return invoicingPolicy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SaleOrderDocument document)) {
            return false;
        }
        return id != null && id.equals(document.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
