package com.valeerp.pos;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Ticket POS (libro). Totales con Money dentro del agregado; ignora subtotales del cliente.
 * Anulación/devolución vía documentos de reverso — sin delete físico.
 */
@Entity
@Table(name = "pos_tickets")
public class PosTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pos_config_id", nullable = false)
    private Long posConfigId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /** Sesión de caja opcional (tesorería por id; no FK cruzada). */
    @Column(name = "cash_session_id")
    private Long cashSessionId;

    @Column(name = "partner_id")
    private Long partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PosTicketStatus status;

    @Column(name = "subtotal_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalBase;

    @Column(name = "tax_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "grand_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal grandTotal;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "void_reason", length = 500)
    private String voidReason;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosTicketLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosTicketPayment> payments = new ArrayList<>();

    protected PosTicket() {
    }

    public static PosTicket open(PosConfig config, Long cashSessionId, Long partnerId, Instant openedAt) {
        if (config == null || config.getId() == null) {
            throw new DomainException("La config POS es obligatoria");
        }
        if (config.isRetired()) {
            throw new DomainException("La config POS está dada de baja");
        }
        PosTicket ticket = new PosTicket();
        ticket.posConfigId = config.getId();
        ticket.companyId = config.getCompanyId();
        ticket.cashSessionId = cashSessionId;
        ticket.partnerId = partnerId;
        ticket.status = PosTicketStatus.DRAFT;
        ticket.currency = config.getCurrency();
        ticket.subtotalBase = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        ticket.taxTotal = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        ticket.grandTotal = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        ticket.openedAt = openedAt == null ? Instant.now() : openedAt;
        return ticket;
    }

    public void addLine(Long productId, String productName, int quantity, Money unitPrice,
                        Long taxRateId, BigDecimal taxRate) {
        assertDraft();
        requireSameCurrency(unitPrice);
        PosTicketLine line = PosTicketLine.of(this, productId, productName, quantity, unitPrice, taxRateId, taxRate);
        lines.add(line);
        recalculateTotals();
    }

    public void addPayment(Long paymentMethodId, Money amount) {
        assertDraft();
        requireSameCurrency(amount);
        payments.add(PosTicketPayment.of(this, paymentMethodId, amount));
    }

    public void post(Instant when) {
        assertDraft();
        if (lines.isEmpty()) {
            throw new DomainException("El ticket no tiene líneas");
        }
        Money paid = paidTotal();
        Money due = totalMoney();
        if (paid.amount().compareTo(due.amount()) != 0) {
            throw new DomainException("Los pagos mixtos deben igualar el total del ticket");
        }
        this.status = PosTicketStatus.POSTED;
        this.postedAt = when == null ? Instant.now() : when;
    }

    /**
     * Anula un ticket publicado creando el hecho de reverso en el servicio;
     * aquí solo cambia estado (libro).
     */
    public void markVoided(String reason, Instant when) {
        if (status != PosTicketStatus.POSTED && status != PosTicketStatus.PARTIALLY_RETURNED) {
            throw new DomainException("Solo se anulan tickets publicados sin devolución total");
        }
        if (status == PosTicketStatus.RETURNED || status == PosTicketStatus.VOIDED) {
            throw new DomainException("El ticket ya está cerrado");
        }
        if (hasAnyReturn()) {
            throw new DomainException("No se puede anular un ticket con devoluciones; use reverso de devolución");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("El motivo de anulación es obligatorio");
        }
        this.status = PosTicketStatus.VOIDED;
        this.voidReason = reason.trim();
        this.voidedAt = when == null ? Instant.now() : when;
    }

    public void applyConfirmedReturn() {
        if (status != PosTicketStatus.POSTED && status != PosTicketStatus.PARTIALLY_RETURNED) {
            throw new DomainException("El ticket no admite devoluciones en estado " + status);
        }
        boolean anyRemaining = lines.stream().anyMatch(line -> line.remainingQuantity() > 0);
        this.status = anyRemaining ? PosTicketStatus.PARTIALLY_RETURNED : PosTicketStatus.RETURNED;
    }

    public PosTicketLine requireLine(Long lineId) {
        return lines.stream()
                .filter(line -> line.getId() != null && line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Línea de ticket no encontrada"));
    }

    public Money totalMoney() {
        return Money.of(grandTotal, currency);
    }

    public Money taxMoney() {
        return Money.of(taxTotal, currency);
    }

    public Money baseMoney() {
        return Money.of(subtotalBase, currency);
    }

    public Money paidTotal() {
        Money sum = Money.zero(currency);
        for (PosTicketPayment payment : payments) {
            sum = sum.plus(payment.money());
        }
        return sum;
    }

    private void recalculateTotals() {
        Money base = Money.zero(currency);
        Money tax = Money.zero(currency);
        Money net = Money.zero(currency);
        for (PosTicketLine line : lines) {
            base = base.plus(line.baseMoney());
            tax = tax.plus(line.taxMoney());
            net = net.plus(line.netMoney());
        }
        this.subtotalBase = base.amount();
        this.taxTotal = tax.amount();
        this.grandTotal = net.amount();
    }

    private boolean hasAnyReturn() {
        return lines.stream().anyMatch(line -> line.getQuantityReturned() > 0);
    }

    private void assertDraft() {
        if (status != PosTicketStatus.DRAFT) {
            throw new DomainException("El ticket ya no está en borrador");
        }
    }

    private void requireSameCurrency(Money money) {
        if (money == null || !currency.equals(money.currency())) {
            throw new DomainException("La moneda del ticket es " + currency);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPosConfigId() {
        return posConfigId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getCashSessionId() {
        return cashSessionId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public PosTicketStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public Instant getVoidedAt() {
        return voidedAt;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public List<PosTicketLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public List<PosTicketPayment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PosTicket ticket)) {
            return false;
        }
        return id != null && id.equals(ticket.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
