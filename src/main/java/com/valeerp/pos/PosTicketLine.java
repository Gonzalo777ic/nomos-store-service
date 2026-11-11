package com.valeerp.pos;

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
@Table(name = "pos_ticket_lines")
public class PosTicketLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private PosTicket ticket;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "quantity_returned", nullable = false)
    private int quantityReturned;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "tax_rate_id", nullable = false)
    private Long taxRateId;

    /** Tasa persistida al momento de la línea (catálogo fiscal), nunca literal suelto. */
    @Column(name = "tax_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal taxRate;

    @Column(name = "line_net", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineNet;

    @Column(name = "line_tax", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTax;

    @Column(name = "line_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineBase;

    @Column(nullable = false, length = 3)
    private String currency;

    protected PosTicketLine() {
    }

    static PosTicketLine of(PosTicket ticket, Long productId, String productName, int quantity,
                            Money unitPrice, Long taxRateId, BigDecimal taxRateValue) {
        if (productId == null) {
            throw new DomainException("productId es obligatorio");
        }
        if (productName == null || productName.isBlank()) {
            throw new DomainException("productName es obligatorio");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad debe ser mayor que cero");
        }
        if (taxRateId == null) {
            throw new DomainException("taxRateId es obligatorio");
        }
        if (taxRateValue == null || taxRateValue.signum() < 0) {
            throw new DomainException("La tasa de impuesto no es válida");
        }
        if (unitPrice == null) {
            throw new DomainException("El precio unitario es obligatorio");
        }

        Money net = unitPrice.times(quantity);
        Money tax = net.includedTax(taxRateValue);
        Money base = net.minus(tax);

        PosTicketLine line = new PosTicketLine();
        line.ticket = ticket;
        line.productId = productId;
        line.productName = productName.trim();
        line.quantity = quantity;
        line.quantityReturned = 0;
        line.unitPrice = unitPrice.amount();
        line.taxRateId = taxRateId;
        line.taxRate = taxRateValue;
        line.lineNet = net.amount();
        line.lineTax = tax.amount();
        line.lineBase = base.amount();
        line.currency = unitPrice.currency();
        return line;
    }

    void registerReturn(int qty) {
        if (qty <= 0) {
            throw new DomainException("La cantidad a devolver debe ser mayor que cero");
        }
        if (quantityReturned + qty > quantity) {
            throw new DomainException("No se puede devolver más de lo vendido en la línea " + id);
        }
        quantityReturned += qty;
    }

    int remainingQuantity() {
        return quantity - quantityReturned;
    }

    Money netMoney() {
        return Money.of(lineNet, currency);
    }

    Money taxMoney() {
        return Money.of(lineTax, currency);
    }

    Money baseMoney() {
        return Money.of(lineBase, currency);
    }

    Money unitPriceMoney() {
        return Money.of(unitPrice, currency);
    }

    Money refundFor(int qty) {
        if (qty <= 0 || qty > remainingQuantity()) {
            throw new DomainException("Cantidad de devolución inválida para la línea " + id);
        }
        return unitPriceMoney().times(qty);
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getQuantityReturned() {
        return quantityReturned;
    }

    public Long getTaxRateId() {
        return taxRateId;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public String getCurrency() {
        return currency;
    }
}
