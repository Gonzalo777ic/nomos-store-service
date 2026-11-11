package com.valeerp.accounting;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cobro o pago. Libro: confirmación y anulación por estado; conciliación vía {@link #reconcile}.
 */
@Entity
@Table(name = "accounting_payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentDirection direction;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal amountValue;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "reference_number", length = 80)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaymentAllocation> allocations = new ArrayList<>();

    protected Payment() {
    }

    public static Payment draft(Long companyId, Long partnerId, PaymentDirection direction,
                                LocalDate paymentDate, Money amount, String referenceNumber) {
        if (companyId == null || partnerId == null) {
            throw new DomainException("El pago exige compañía y partner");
        }
        if (direction == null) {
            throw new DomainException("La dirección del pago es obligatoria");
        }
        if (paymentDate == null) {
            throw new DomainException("La fecha del pago es obligatoria");
        }
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("El monto del pago debe ser positivo");
        }
        Payment payment = new Payment();
        payment.companyId = companyId;
        payment.partnerId = partnerId;
        payment.direction = direction;
        payment.paymentDate = paymentDate;
        payment.amountValue = amount.amount();
        payment.currencyCode = amount.currency();
        payment.referenceNumber = blankToNull(referenceNumber);
        payment.status = PaymentStatus.DRAFT;
        return payment;
    }

    public void confirm(Instant when) {
        if (status != PaymentStatus.DRAFT) {
            throw new DomainException("Solo un pago en borrador se puede confirmar");
        }
        this.status = PaymentStatus.CONFIRMED;
        this.confirmedAt = when;
    }

    public void cancel() {
        if (status == PaymentStatus.CANCELLED) {
            throw new DomainException("El pago ya está cancelado");
        }
        if (!allocations.isEmpty()) {
            throw new DomainException("No se puede anular un pago conciliado; revierta asignaciones primero");
        }
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * Conciliación (#76): aplica montos del pago confirmado a facturas del mismo partner/lado.
     *
     * @param invoicesById facturas cargadas por id (el caso de uso resuelve el mapa)
     * @param amountsByInvoiceId montos a aplicar por factura
     */
    public List<PaymentAllocation> reconcile(Map<Long, InvoiceDocument> invoicesById,
                                             Map<Long, Money> amountsByInvoiceId) {
        if (status != PaymentStatus.CONFIRMED) {
            throw new DomainException("Solo un pago confirmado admite conciliación");
        }
        if (amountsByInvoiceId == null || amountsByInvoiceId.isEmpty()) {
            throw new DomainException("Debe indicar al menos una factura a conciliar");
        }

        Money planned = Money.zero(currencyCode);
        for (Map.Entry<Long, Money> entry : amountsByInvoiceId.entrySet()) {
            Money applyAmount = entry.getValue();
            if (applyAmount == null || !applyAmount.isPositive()) {
                throw new DomainException("Cada monto a conciliar debe ser positivo");
            }
            if (!currencyCode.equals(applyAmount.currency())) {
                throw new DomainException("La moneda a conciliar no coincide con el pago");
            }
            planned = planned.plus(applyAmount);
        }
        if (planned.greaterThan(unallocated())) {
            throw new DomainException("La suma a conciliar supera el monto disponible del pago");
        }

        List<PaymentAllocation> created = new ArrayList<>();
        for (Map.Entry<Long, Money> entry : amountsByInvoiceId.entrySet()) {
            Long invoiceId = entry.getKey();
            InvoiceDocument invoice = invoicesById == null ? null : invoicesById.get(invoiceId);
            if (invoice == null) {
                throw new DomainException("Factura no encontrada para conciliar: " + invoiceId);
            }
            if (!Objects.equals(invoice.getCompanyId(), companyId)) {
                throw new DomainException("La factura no pertenece a la misma compañía");
            }
            created.add(invoice.applyPayment(this, entry.getValue()));
        }
        return Collections.unmodifiableList(created);
    }

    public Money amount() {
        return Money.of(amountValue, currencyCode);
    }

    /**
     * Monto ya asignado excluyendo (no aplica: allocations se borran al cancelar pago).
     */
    public Money allocatedAmount() {
        Money allocated = Money.zero(currencyCode);
        for (PaymentAllocation allocation : allocations) {
            allocated = allocated.plus(allocation.amount());
        }
        return allocated;
    }

    public Money unallocated() {
        return amount().minus(allocatedAmount());
    }

    void registerAllocation(PaymentAllocation allocation) {
        if (allocation != null && !allocations.contains(allocation)) {
            allocations.add(allocation);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public PaymentDirection getDirection() {
        return direction;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public List<PaymentAllocation> getAllocations() {
        return Collections.unmodifiableList(allocations);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Payment payment) || id == null) {
            return false;
        }
        return id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
