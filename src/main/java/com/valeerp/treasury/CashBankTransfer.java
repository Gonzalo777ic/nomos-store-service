package com.valeerp.treasury;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Documento de extracción/depósito entre caja y banco.
 * Al publicar genera movimientos del libro; anulación por compensación, no delete.
 */
@Entity
@Table(name = "cash_bank_transfers")
public class CashBankTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TransferDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransferStatus status;

    @Column(name = "cash_register_id", nullable = false)
    private Long cashRegisterId;

    @Column(name = "cash_session_id")
    private Long cashSessionId;

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(length = TreasuryPolicy.CONCEPT_MAX_LENGTH)
    private String concept;

    @Column(name = "cash_movement_id")
    private Long cashMovementId;

    @Column(name = "bank_movement_id")
    private Long bankMovementId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    protected CashBankTransfer() {
    }

    public static CashBankTransfer draft(Long companyId, TransferDirection direction, Long cashRegisterId,
                                         Long cashSessionId, Long bankAccountId, Money money,
                                         String concept, Actor actor, Instant when) {
        if (companyId == null) {
            throw new DomainException("La transferencia exige compañía");
        }
        if (direction == null) {
            throw new DomainException("El sentido de la transferencia es obligatorio");
        }
        if (cashRegisterId == null) {
            throw new DomainException("La transferencia exige caja");
        }
        if (bankAccountId == null) {
            throw new DomainException("La transferencia exige cuenta bancaria (id local)");
        }
        Money value = requirePositive(money);
        Actor resolved = actor == null ? Actor.SYSTEM : actor;
        CashBankTransfer transfer = new CashBankTransfer();
        transfer.companyId = companyId;
        transfer.direction = direction;
        transfer.status = TransferStatus.DRAFT;
        transfer.cashRegisterId = cashRegisterId;
        transfer.cashSessionId = cashSessionId;
        transfer.bankAccountId = bankAccountId;
        transfer.amount = value.amount();
        transfer.currencyCode = value.currency();
        transfer.concept = requireConcept(concept);
        transfer.createdAt = when == null ? Instant.now() : when;
        transfer.createdBy = resolved.id();
        return transfer;
    }

    public void markPosted(Long cashMovementId, Long bankMovementId, Instant when) {
        if (status != TransferStatus.DRAFT) {
            throw new DomainException("Solo un borrador se puede publicar");
        }
        if (cashMovementId == null || bankMovementId == null) {
            throw new DomainException("La publicación exige ambos movimientos de libro");
        }
        this.cashMovementId = cashMovementId;
        this.bankMovementId = bankMovementId;
        this.status = TransferStatus.POSTED;
        this.postedAt = when == null ? Instant.now() : when;
    }

    public void markVoided(Instant when) {
        if (status != TransferStatus.POSTED) {
            throw new DomainException("Solo una transferencia publicada se puede anular");
        }
        this.status = TransferStatus.VOIDED;
        this.voidedAt = when == null ? Instant.now() : when;
    }

    public Money money() {
        return Money.of(amount, currencyCode);
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public TransferDirection getDirection() {
        return direction;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public Long getCashRegisterId() {
        return cashRegisterId;
    }

    public Long getCashSessionId() {
        return cashSessionId;
    }

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public String getConcept() {
        return concept;
    }

    public Long getCashMovementId() {
        return cashMovementId;
    }

    public Long getBankMovementId() {
        return bankMovementId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public Instant getVoidedAt() {
        return voidedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    private static Money requirePositive(Money money) {
        if (money == null || !money.isPositive()) {
            throw new DomainException("El monto de la transferencia debe ser positivo");
        }
        return money;
    }

    private static String requireConcept(String concept) {
        if (concept == null || concept.isBlank()) {
            throw new DomainException("El concepto de la transferencia es obligatorio");
        }
        String trimmed = concept.trim();
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
        if (!(other instanceof CashBankTransfer transfer)) {
            return false;
        }
        return id != null && id.equals(transfer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
