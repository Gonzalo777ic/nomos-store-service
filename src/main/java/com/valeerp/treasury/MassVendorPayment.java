package com.valeerp.treasury;

import com.valeerp.kernel.Actor;
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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lote de pagos masivos a proveedores. Agregado con estados; anulación por cancelación/reverso.
 */
@Entity
@Table(name = "mass_vendor_payments")
public class MassVendorPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = TreasuryPolicy.CODE_MAX_LENGTH)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private MassPaymentStatus status;

    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "cash_register_id")
    private Long cashRegisterId;

    @Column(name = "cash_session_id")
    private Long cashSessionId;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal totalAmount;

    @Column(length = TreasuryPolicy.CONCEPT_MAX_LENGTH)
    private String concept;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MassVendorPaymentLine> lines = new ArrayList<>();

    protected MassVendorPayment() {
    }

    public static MassVendorPayment draft(Long companyId, String reference, Long paymentMethodId,
                                          Long bankAccountId, Long cashRegisterId, Long cashSessionId,
                                          String currencyCode, String concept, Actor actor, Instant when) {
        if (companyId == null) {
            throw new DomainException("El lote exige compañía");
        }
        if (paymentMethodId == null) {
            throw new DomainException("El lote exige método de pago");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new DomainException("La moneda del lote es obligatoria");
        }
        String ref = reference == null || reference.isBlank()
                ? "MP-" + Instant.now().toEpochMilli()
                : reference.trim();
        Actor resolved = actor == null ? Actor.SYSTEM : actor;
        MassVendorPayment batch = new MassVendorPayment();
        batch.companyId = companyId;
        batch.reference = ref;
        batch.status = MassPaymentStatus.DRAFT;
        batch.paymentMethodId = paymentMethodId;
        batch.bankAccountId = bankAccountId;
        batch.cashRegisterId = cashRegisterId;
        batch.cashSessionId = cashSessionId;
        batch.currencyCode = currencyCode.trim().toUpperCase();
        batch.totalAmount = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        batch.concept = blankToNull(concept);
        batch.createdAt = when == null ? Instant.now() : when;
        batch.createdBy = resolved.id();
        return batch;
    }

    public MassVendorPaymentLine addLine(Long partnerId, Long payableDocumentId, Money amount, String lineConcept) {
        requireDraft();
        Money value = requirePositive(amount);
        if (!currencyCode.equals(value.currency())) {
            throw new DomainException("La moneda de la línea no coincide con el lote");
        }
        if (partnerId == null) {
            throw new DomainException("La línea exige partnerId de proveedor");
        }
        MassVendorPaymentLine line = MassVendorPaymentLine.create(this, partnerId, payableDocumentId, value, lineConcept);
        lines.add(line);
        recalculateTotal();
        return line;
    }

    public void confirm(Instant when) {
        requireDraft();
        if (lines.isEmpty()) {
            throw new DomainException("El lote exige al menos una línea");
        }
        this.status = MassPaymentStatus.CONFIRMED;
        this.confirmedAt = when == null ? Instant.now() : when;
    }

    public void markPosted(Instant when) {
        if (status != MassPaymentStatus.CONFIRMED) {
            throw new DomainException("Solo un lote confirmado se puede contabilizar");
        }
        this.status = MassPaymentStatus.POSTED;
        this.postedAt = when == null ? Instant.now() : when;
    }

    public void cancel(Instant when) {
        if (status == MassPaymentStatus.POSTED) {
            throw new DomainException("Un lote contabilizado se anula con reverso de movimientos, no con cancelación");
        }
        if (status == MassPaymentStatus.CANCELLED) {
            throw new DomainException("El lote ya está cancelado");
        }
        this.status = MassPaymentStatus.CANCELLED;
        this.cancelledAt = when == null ? Instant.now() : when;
    }

    public Money total() {
        return Money.of(totalAmount, currencyCode);
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getReference() {
        return reference;
    }

    public MassPaymentStatus getStatus() {
        return status;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public Long getCashRegisterId() {
        return cashRegisterId;
    }

    public Long getCashSessionId() {
        return cashSessionId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getConcept() {
        return concept;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<MassVendorPaymentLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private void recalculateTotal() {
        Money sum = Money.zero(currencyCode);
        for (MassVendorPaymentLine line : lines) {
            sum = sum.plus(line.amount());
        }
        this.totalAmount = sum.amount();
    }

    private void requireDraft() {
        if (status != MassPaymentStatus.DRAFT) {
            throw new DomainException("Solo un lote en borrador admite cambios");
        }
    }

    private static Money requirePositive(Money money) {
        if (money == null || !money.isPositive()) {
            throw new DomainException("El monto de la línea debe ser positivo");
        }
        return money;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > TreasuryPolicy.CONCEPT_MAX_LENGTH) {
            throw new DomainException("El concepto excede la longitud permitida");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MassVendorPayment payment)) {
            return false;
        }
        return id != null && id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
