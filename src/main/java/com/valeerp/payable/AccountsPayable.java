package com.valeerp.payable;

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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Cuentas por pagar (#75), simétricas a CxC. Anulación por reverso (VOIDED), nunca delete.
 * {@link #paidAmount()} excluye pagos CANCELLED.
 */
@Entity
@Table(name = "accounts_payable")
public class AccountsPayable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "supplier_invoice_id")
    private Long supplierInvoiceId;

    @Column(name = "document_number", nullable = false, length = 64)
    private String documentNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PayableStatus status;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "reversal_journal_entry_id")
    private Long reversalJournalEntryId;

    @OneToMany(mappedBy = "accountsPayable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PayableInstallment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "accountsPayable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PayablePayment> payments = new ArrayList<>();

    protected AccountsPayable() {
    }

    public static AccountsPayable open(Long companyId, Long partnerId, Long supplierInvoiceId,
                                       String documentNumber, LocalDate issueDate, Money total) {
        if (companyId == null || partnerId == null) {
            throw new DomainException("La CxP exige compañía y partner");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new DomainException("El número de documento CxP es obligatorio");
        }
        if (issueDate == null) {
            throw new DomainException("La fecha de emisión CxP es obligatoria");
        }
        if (total == null || !total.isPositive()) {
            throw new DomainException("El total de la CxP debe ser positivo");
        }
        AccountsPayable payable = new AccountsPayable();
        payable.companyId = companyId;
        payable.partnerId = partnerId;
        payable.supplierInvoiceId = supplierInvoiceId;
        payable.documentNumber = documentNumber.trim();
        payable.issueDate = issueDate;
        payable.totalAmount = total.amount();
        payable.currencyCode = total.currency();
        payable.status = PayableStatus.ACTIVE;
        return payable;
    }

    public void addInstallment(PayableInstallment installment) {
        if (installment == null) {
            throw new DomainException("La cuota CxP es obligatoria");
        }
        installment.attachTo(this);
        installments.add(installment);
    }

    public void scheduleSingleInstallment(LocalDate dueDate) {
        addInstallment(PayableInstallment.schedule(1, dueDate == null ? issueDate : dueDate, total()));
    }

    public PayablePayment applyPayment(LocalDate paymentDate, Money amount, String referenceNumber) {
        assertActive();
        PayablePayment payment = PayablePayment.confirm(paymentDate, amount, referenceNumber);
        payment.attachTo(this);
        payments.add(payment);
        Money remaining = amount;
        List<PayableInstallment> pending = installments.stream()
                .filter(i -> i.getStatus() != PayableInstallmentStatus.PAID)
                .sorted(Comparator.comparing(PayableInstallment::getDueDate))
                .toList();
        for (PayableInstallment installment : pending) {
            if (!remaining.isPositive()) {
                break;
            }
            remaining = installment.applyPayment(remaining);
        }
        refreshStatus();
        return payment;
    }

    public void cancelPayment(Long paymentId) {
        assertActive();
        PayablePayment payment = payments.stream()
                .filter(p -> Objects.equals(p.getId(), paymentId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Pago CxP no encontrado"));
        payment.cancel();
        refreshStatus();
    }

    /**
     * Anulación por reverso: marca VOIDED y conserva el histórico; el caso de uso enlaza el contra-asiento.
     */
    public void voidByReversal(Instant when, Long reversalJournalEntryId) {
        if (status == PayableStatus.VOIDED) {
            throw new DomainException("La CxP ya fue anulada");
        }
        if (paidAmount().isPositive()) {
            throw new DomainException("No se anula una CxP con pagos confirmados; cancele pagos primero");
        }
        this.status = PayableStatus.VOIDED;
        this.voidedAt = when;
        this.reversalJournalEntryId = reversalJournalEntryId;
    }

    /** Excluye pagos en estado CANCELLED. */
    public Money paidAmount() {
        Money paid = Money.zero(currencyCode);
        for (PayablePayment payment : payments) {
            if (payment.getStatus() == PayablePaymentStatus.CANCELLED) {
                continue;
            }
            paid = paid.plus(payment.money());
        }
        return paid;
    }

    public Money balance() {
        return total().minus(paidAmount());
    }

    public Money total() {
        return Money.of(totalAmount, currencyCode);
    }

    private void assertActive() {
        if (status == PayableStatus.VOIDED) {
            throw new DomainException("La CxP está anulada");
        }
    }

    private void refreshStatus() {
        if (status == PayableStatus.VOIDED) {
            return;
        }
        boolean allPaid = !installments.isEmpty()
                && installments.stream().allMatch(i -> i.getStatus() == PayableInstallmentStatus.PAID);
        this.status = allPaid ? PayableStatus.PAID : PayableStatus.ACTIVE;
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

    public Long getSupplierInvoiceId() {
        return supplierInvoiceId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public PayableStatus getStatus() {
        return status;
    }

    public Instant getVoidedAt() {
        return voidedAt;
    }

    public Long getReversalJournalEntryId() {
        return reversalJournalEntryId;
    }

    public List<PayableInstallment> getInstallments() {
        return Collections.unmodifiableList(installments);
    }

    public List<PayablePayment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AccountsPayable payable) || id == null) {
            return false;
        }
        return id.equals(payable.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
