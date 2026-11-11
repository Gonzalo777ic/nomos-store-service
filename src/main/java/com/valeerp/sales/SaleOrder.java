package com.valeerp.sales;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cotización / pedido de venta. Totales en Money dentro del agregado; confirm() emite documento comercial.
 */
@Entity
@Table(name = "sale_orders")
public class SaleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "opportunity_id")
    private Long opportunityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SaleOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoicing_policy", nullable = false, length = 24)
    private InvoicingPolicy invoicingPolicy;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "global_discount_kind", length = 24)
    private DiscountKind globalDiscountKind;

    @Column(name = "global_discount_value", precision = 19, scale = 4)
    private BigDecimal globalDiscountValue;

    @Column(name = "global_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal globalDiscountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "sales_team_id")
    private Long salesTeamId;

    @Column(name = "territory_id")
    private Long territoryId;

    @Column(name = "commission_rule_id")
    private Long commissionRuleId;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @OneToMany(mappedBy = "saleOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SaleOrderLine> lines = new ArrayList<>();

    @OneToOne(mappedBy = "saleOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private SaleOrderDocument commercialDocument;

    protected SaleOrder() {
    }

    public static SaleOrder draft(Long partnerId, Long opportunityId, String currency,
                                  Instant validUntil, InvoicingPolicy invoicingPolicy,
                                  String notes, Instant createdAt) {
        if (partnerId == null) {
            throw new DomainException("La cotización exige un partnerId");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainException("La moneda es obligatoria");
        }
        if (validUntil == null) {
            throw new DomainException("La vigencia de la cotización es obligatoria");
        }
        if (createdAt == null) {
            throw new DomainException("La fecha de creación es obligatoria");
        }
        if (validUntil.isBefore(createdAt)) {
            throw new DomainException("La vigencia no puede ser anterior a la creación");
        }

        SaleOrder order = new SaleOrder();
        order.partnerId = partnerId;
        order.opportunityId = opportunityId;
        order.status = SaleOrderStatus.DRAFT;
        order.invoicingPolicy = invoicingPolicy == null ? InvoicingPolicy.ORDERED : invoicingPolicy;
        order.currency = currency.trim().toUpperCase();
        order.validUntil = validUntil;
        order.notes = blankToNull(notes);
        order.createdAt = createdAt;
        order.subtotalAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        order.discountAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        order.globalDiscountAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        order.taxAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        order.baseAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        order.totalAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        return order;
    }

    public SaleOrderLine addLine(Long productId, String productName, int quantity, Money unitPrice) {
        return addLine(productId, productName, quantity, unitPrice, null, Discount.none(), null, BigDecimal.ZERO);
    }

    public SaleOrderLine addLine(Long productId, String productName, int quantity, Money unitPrice,
                                 PromotionSpec promotion) {
        return addLine(productId, productName, quantity, unitPrice, promotion, Discount.none(), null, BigDecimal.ZERO);
    }

    public SaleOrderLine addLine(Long productId, String productName, int quantity, Money unitPrice,
                                 PromotionSpec promotion, Discount explicitDiscount,
                                 Long taxRateId, BigDecimal taxRateDecimal) {
        assertDraft();
        // El subtotal que pudiera enviar el cliente se ignora: solo qty × precio + política fiscal.
        SaleOrderLine line = SaleOrderLine.create(
                this, productId, productName, quantity, unitPrice, promotion,
                explicitDiscount, taxRateId, taxRateDecimal == null ? BigDecimal.ZERO : taxRateDecimal
        );
        lines.add(line);
        recalculateTotals();
        return line;
    }

    public void changeLineQuantity(Long lineId, int quantity) {
        assertDraft();
        requireLine(lineId).changeQuantity(quantity);
        recalculateTotals();
    }

    public void changeLineUnitPrice(Long lineId, Money unitPrice) {
        assertDraft();
        requireLine(lineId).changeUnitPrice(unitPrice);
        recalculateTotals();
    }

    public void applyPromotionToLine(Long lineId, PromotionSpec promotion) {
        assertDraft();
        if (promotion == null) {
            throw new DomainException("La promoción es obligatoria");
        }
        requireLine(lineId).applyPromotion(promotion);
        recalculateTotals();
    }

    public void clearLinePromotion(Long lineId) {
        assertDraft();
        requireLine(lineId).clearPromotion();
        recalculateTotals();
    }

    public void applyLineDiscount(Long lineId, Discount discount) {
        assertDraft();
        requireLine(lineId).applyExplicitDiscount(discount == null ? Discount.none() : discount);
        recalculateTotals();
    }

    public void applyGlobalDiscount(Discount discount) {
        assertDraft();
        if (discount == null || discount.isNone()) {
            this.globalDiscountKind = null;
            this.globalDiscountValue = null;
        } else {
            this.globalDiscountKind = discount.kind();
            this.globalDiscountValue = discount.value();
        }
        recalculateTotals();
    }

    public void assignCommercialContext(Long sellerId, Long salesTeamId, Long territoryId, Long commissionRuleId) {
        assertDraft();
        this.sellerId = sellerId;
        this.salesTeamId = salesTeamId;
        this.territoryId = territoryId;
        this.commissionRuleId = commissionRuleId;
    }

    public void removeLine(Long lineId) {
        assertDraft();
        SaleOrderLine line = requireLine(lineId);
        lines.remove(line);
        recalculateTotals();
    }

    public void changeValidity(Instant validUntil) {
        assertDraft();
        if (validUntil == null) {
            throw new DomainException("La vigencia es obligatoria");
        }
        if (validUntil.isBefore(createdAt)) {
            throw new DomainException("La vigencia no puede ser anterior a la creación");
        }
        this.validUntil = validUntil;
    }

    public void changeInvoicingPolicy(InvoicingPolicy policy) {
        assertDraft();
        if (policy == null) {
            throw new DomainException("La política de facturación es obligatoria");
        }
        this.invoicingPolicy = policy;
    }

    public void changeNotes(String notes) {
        assertDraft();
        this.notes = blankToNull(notes);
    }

    /**
     * Confirma el pedido y genera el documento comercial (no factura fiscal).
     */
    public SaleOrderDocument confirm(String documentNumber, Instant when) {
        assertDraft();
        if (lines.isEmpty()) {
            throw new DomainException("El pedido no tiene líneas para confirmar");
        }
        if (when == null) {
            throw new DomainException("La fecha de confirmación es obligatoria");
        }
        if (when.isAfter(validUntil)) {
            throw new DomainException("La cotización está vencida; no se puede confirmar");
        }
        if (commercialDocument != null) {
            throw new DomainException("El pedido ya tiene documento comercial");
        }
        this.status = SaleOrderStatus.CONFIRMED;
        this.confirmedAt = when;
        this.commercialDocument = SaleOrderDocument.issue(
                this,
                SalesPolicy.SALE_ORDER_DOCUMENT_SERIES,
                documentNumber,
                when
        );
        return commercialDocument;
    }

    public void cancel(Instant when) {
        if (status == SaleOrderStatus.CANCELLED) {
            throw new DomainException("El pedido ya está cancelado");
        }
        if (status == SaleOrderStatus.CONFIRMED) {
            throw new DomainException("Un pedido confirmado no se cancela aquí; use reverso/documento compensatorio");
        }
        this.status = SaleOrderStatus.CANCELLED;
        this.cancelledAt = when;
    }

    public Money subtotal() {
        return Money.of(subtotalAmount, currency);
    }

    public Money discount() {
        return Money.of(discountAmount, currency);
    }

    public Money globalDiscount() {
        return Money.of(globalDiscountAmount, currency);
    }

    public Money tax() {
        return Money.of(taxAmount, currency);
    }

    public Money base() {
        return Money.of(baseAmount, currency);
    }

    public Money total() {
        return Money.of(totalAmount, currency);
    }

    public Long getId() {
        return id;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public SaleOrderStatus getStatus() {
        return status;
    }

    public InvoicingPolicy getInvoicingPolicy() {
        return invoicingPolicy;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public DiscountKind getGlobalDiscountKind() {
        return globalDiscountKind;
    }

    public BigDecimal getGlobalDiscountValue() {
        return globalDiscountValue;
    }

    public BigDecimal getGlobalDiscountAmount() {
        return globalDiscountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getSalesTeamId() {
        return salesTeamId;
    }

    public Long getTerritoryId() {
        return territoryId;
    }

    public Long getCommissionRuleId() {
        return commissionRuleId;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public List<SaleOrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public SaleOrderDocument getCommercialDocument() {
        return commercialDocument;
    }

    private SaleOrderLine requireLine(Long lineId) {
        if (lineId == null) {
            throw new DomainException("La línea es obligatoria");
        }
        return lines.stream()
                .filter(line -> lineId.equals(line.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("NOT_FOUND", "Línea de pedido no encontrada"));
    }

    private void recalculateTotals() {
        Money subtotal = Money.zero(currency);
        Money lineDiscounts = Money.zero(currency);
        if (lines.isEmpty()) {
            this.subtotalAmount = subtotal.amount();
            this.discountAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
            this.globalDiscountAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
            this.taxAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
            this.baseAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
            this.totalAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
            return;
        }
        java.util.List<Money> weights = new java.util.ArrayList<>(lines.size());
        for (SaleOrderLine line : lines) {
            line.recalculatePreGlobal();
            subtotal = subtotal.plus(line.grossMoney());
            lineDiscounts = lineDiscounts.plus(line.promotionAndLineDiscountMoney());
            weights.add(line.preGlobalNetMoney());
        }
        Money preGlobalNet = Money.zero(currency);
        for (Money weight : weights) {
            preGlobalNet = preGlobalNet.plus(weight);
        }
        Discount global = globalDiscountKind == null
                ? Discount.none()
                : Discount.of(globalDiscountKind, globalDiscountValue);
        Money globalAmount = global.applyTo(preGlobalNet).cappedAt(preGlobalNet);
        java.util.List<Money> shares = MoneyAllocator.allocate(globalAmount, weights);

        Money tax = Money.zero(currency);
        Money base = Money.zero(currency);
        Money total = Money.zero(currency);
        Money appliedGlobal = Money.zero(currency);
        for (int i = 0; i < lines.size(); i++) {
            SaleOrderLine line = lines.get(i);
            line.applyGlobalShareAndTax(shares.get(i));
            tax = tax.plus(line.taxMoney());
            base = base.plus(Money.of(line.getBaseAmount(), currency));
            total = total.plus(line.totalMoney());
            appliedGlobal = appliedGlobal.plus(Money.of(line.getGlobalDiscountShare(), currency));
        }

        Money expectedNet = subtotal.minus(lineDiscounts).minus(appliedGlobal);
        if (expectedNet.compareTo(base) != 0) {
            throw new DomainException("Invariante de totales: base != bruto - descuentos");
        }
        if (base.plus(tax).compareTo(total) != 0) {
            throw new DomainException("Invariante de totales: total != base + impuesto");
        }

        this.subtotalAmount = subtotal.amount();
        this.discountAmount = lineDiscounts.plus(appliedGlobal).amount();
        this.globalDiscountAmount = appliedGlobal.amount();
        this.taxAmount = tax.amount();
        this.baseAmount = base.amount();
        this.totalAmount = total.amount();
    }

    private void assertDraft() {
        if (!status.isDraft()) {
            throw new DomainException("Solo una cotización en borrador admite cambios (" + status.name() + ")");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SaleOrder order)) {
            return false;
        }
        return id != null && id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
