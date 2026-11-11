package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
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
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Línea del pedido/cotización. Totales derivados; el subtotal del cliente se ignora.
 */
@Entity
@Table(name = "sale_order_lines")
public class SaleOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_order_id", nullable = false)
    private SaleOrder saleOrder;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_gross", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineGross;

    @Column(name = "line_discount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineDiscount;

    @Column(name = "explicit_discount_kind", length = 24)
    @Enumerated(EnumType.STRING)
    private DiscountKind explicitDiscountKind;

    @Column(name = "explicit_discount_value", precision = 19, scale = 4)
    private BigDecimal explicitDiscountValue;

    @Column(name = "global_discount_share", nullable = false, precision = 19, scale = 2)
    private BigDecimal globalDiscountShare = BigDecimal.ZERO;

    @Column(name = "tax_rate_id")
    private Long taxRateId;

    /** Fracción decimal persistida desde el catálogo fiscal (nunca literal mágico). */
    @Column(name = "tax_rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "promotion_id")
    private Long promotionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_kind", length = 24)
    private PromotionKind promotionKind;

    @Column(name = "promotion_value", precision = 19, scale = 4)
    private BigDecimal promotionValue;

    protected SaleOrderLine() {
    }

    static SaleOrderLine create(SaleOrder saleOrder, Long productId, String productName,
                                int quantity, Money unitPrice, PromotionSpec promotion,
                                Discount explicitDiscount, Long taxRateId, BigDecimal taxRateDecimal) {
        if (saleOrder == null) {
            throw new DomainException("La línea exige un pedido");
        }
        if (productId == null) {
            throw new DomainException("El producto de la línea es obligatorio");
        }
        if (productName == null || productName.isBlank()) {
            throw new DomainException("El nombre del producto es obligatorio");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad debe ser mayor que cero");
        }
        if (unitPrice == null) {
            throw new DomainException("El precio unitario es obligatorio");
        }
        if (!saleOrder.getCurrency().equals(unitPrice.currency())) {
            throw new DomainException("La moneda de la línea no coincide con la del pedido");
        }
        if (taxRateDecimal == null || taxRateDecimal.signum() < 0) {
            throw new DomainException("La tasa de impuesto de la línea no es válida");
        }

        SaleOrderLine line = new SaleOrderLine();
        line.saleOrder = saleOrder;
        line.productId = productId;
        line.productName = productName.trim();
        line.quantity = quantity;
        line.unitPrice = unitPrice.amount();
        line.taxRateId = taxRateId;
        line.taxRate = taxRateDecimal;
        line.applyPromotionSnapshot(promotion);
        line.applyExplicitDiscountSnapshot(explicitDiscount);
        line.recalculatePreGlobal();
        return line;
    }

    void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("La cantidad debe ser mayor que cero");
        }
        this.quantity = quantity;
        recalculatePreGlobal();
    }

    void changeUnitPrice(Money unitPrice) {
        if (unitPrice == null) {
            throw new DomainException("El precio unitario es obligatorio");
        }
        if (!saleOrder.getCurrency().equals(unitPrice.currency())) {
            throw new DomainException("La moneda de la línea no coincide con la del pedido");
        }
        this.unitPrice = unitPrice.amount();
        recalculatePreGlobal();
    }

    void applyPromotion(PromotionSpec promotion) {
        applyPromotionSnapshot(promotion);
        recalculatePreGlobal();
    }

    void clearPromotion() {
        this.promotionId = null;
        this.promotionKind = null;
        this.promotionValue = null;
        recalculatePreGlobal();
    }

    void applyExplicitDiscount(Discount discount) {
        applyExplicitDiscountSnapshot(discount);
        recalculatePreGlobal();
    }

    void assignTaxRate(Long taxRateId, BigDecimal taxRateDecimal) {
        if (taxRateDecimal == null || taxRateDecimal.signum() < 0) {
            throw new DomainException("La tasa de impuesto de la línea no es válida");
        }
        this.taxRateId = taxRateId;
        this.taxRate = taxRateDecimal;
        recalculatePreGlobal();
    }

    /**
     * Aplica la cuota de descuento global y calcula el impuesto con la tasa del catálogo fiscal.
     */
    void applyGlobalShareAndTax(Money globalShare) {
        Money preGlobal = Money.of(this.baseAmount, saleOrder.getCurrency());
        Money share = globalShare == null ? Money.zero(saleOrder.getCurrency()) : globalShare.cappedAt(preGlobal);
        Money untaxed = preGlobal.minus(share);
        Money tax = untaxed.times(taxRate);
        this.globalDiscountShare = share.amount();
        this.baseAmount = untaxed.amount();
        this.taxAmount = tax.amount();
        this.lineTotal = untaxed.plus(tax).amount();
    }

    Money unitPriceMoney() {
        return Money.of(unitPrice, saleOrder.getCurrency());
    }

    Money grossMoney() {
        return Money.of(lineGross, saleOrder.getCurrency());
    }

    Money discountMoney() {
        return Money.of(lineDiscount, saleOrder.getCurrency()).plus(Money.of(globalDiscountShare, saleOrder.getCurrency()));
    }

    Money promotionAndLineDiscountMoney() {
        return Money.of(lineDiscount, saleOrder.getCurrency());
    }

    Money preGlobalNetMoney() {
        return Money.of(baseAmount, saleOrder.getCurrency());
    }

    Money taxMoney() {
        return Money.of(taxAmount, saleOrder.getCurrency());
    }

    Money totalMoney() {
        return Money.of(lineTotal, saleOrder.getCurrency());
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineGross() {
        return lineGross;
    }

    public BigDecimal getLineDiscount() {
        return lineDiscount;
    }

    public DiscountKind getExplicitDiscountKind() {
        return explicitDiscountKind;
    }

    public BigDecimal getExplicitDiscountValue() {
        return explicitDiscountValue;
    }

    public BigDecimal getGlobalDiscountShare() {
        return globalDiscountShare;
    }

    public Long getTaxRateId() {
        return taxRateId;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public PromotionKind getPromotionKind() {
        return promotionKind;
    }

    public BigDecimal getPromotionValue() {
        return promotionValue;
    }

    private void applyPromotionSnapshot(PromotionSpec promotion) {
        if (promotion == null) {
            this.promotionId = null;
            this.promotionKind = null;
            this.promotionValue = null;
            return;
        }
        this.promotionId = promotion.promotionId();
        this.promotionKind = promotion.kind();
        this.promotionValue = promotion.discountValue();
    }

    private void applyExplicitDiscountSnapshot(Discount discount) {
        if (discount == null || discount.isNone()) {
            this.explicitDiscountKind = null;
            this.explicitDiscountValue = null;
            return;
        }
        this.explicitDiscountKind = discount.kind();
        this.explicitDiscountValue = discount.value();
    }

    /**
     * Calcula bruto, descuentos de línea/promo y neto imponible previo al descuento global.
     * Guarda el neto en {@code baseAmount} temporalmente hasta {@link #applyGlobalShareAndTax}.
     */
    void recalculatePreGlobal() {
        Money unit = unitPriceMoney();
        Money gross = unit.times(quantity);
        PromotionSpec spec = promotionKind == null
                ? null
                : new PromotionSpec(promotionId, promotionKind, promotionValue, saleOrder.getCurrency());
        Money promoDiscount = LinePromotionCalculator.discountOf(unit, quantity, spec);
        Money afterPromo = gross.minus(promoDiscount.cappedAt(gross));
        Discount explicit = explicitDiscountKind == null
                ? Discount.none()
                : Discount.of(explicitDiscountKind, explicitDiscountValue);
        Money explicitDisc = explicit.applyTo(afterPromo);
        Money untaxed = afterPromo.minus(explicitDisc);
        Money lineDiscTotal = promoDiscount.plus(explicitDisc);
        this.lineGross = gross.amount();
        this.lineDiscount = lineDiscTotal.amount();
        this.globalDiscountShare = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        this.baseAmount = untaxed.amount();
        this.taxAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        this.lineTotal = untaxed.amount();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SaleOrderLine line)) {
            return false;
        }
        return id != null && id.equals(line.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
