package com.nomos.store.service.model;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.treasury.CashChannel;
import com.valeerp.treasury.TreasuryPolicy;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Libro de movimientos de efectivo/banco. No se oculta ni borra; anulación por compensación.
 */
@Entity
@Table(name = "cash_movements")
public class CashMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CashMovementType type;

    @Column(nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CashChannel channel;

    @Column(name = "cash_register_id")
    private Long cashRegisterId;

    @Column(name = "cash_session_id")
    private Long cashSessionId;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethodConfig paymentMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Column(name = "external_reference", length = TreasuryPolicy.REFERENCE_MAX_LENGTH)
    private String externalReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CashMovementStatus status;

    @Column(length = TreasuryPolicy.CONCEPT_MAX_LENGTH)
    private String concept;

    @Column(name = "source_document_type", length = 64)
    private String sourceDocumentType;

    @Column(name = "source_document_id")
    private Long sourceDocumentId;

    @Column(name = "compensates_movement_id")
    private Long compensatesMovementId;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CashMovement() {
    }

    public static CashMovement record(CashMovementType type, Money money, CashChannel channel,
                                      PaymentMethodConfig paymentMethod, Long cashRegisterId,
                                      Long cashSessionId, Long bankAccountId, String concept,
                                      String externalReference, String sourceDocumentType,
                                      Long sourceDocumentId, Actor actor, Instant when) {
        if (type == null) {
            throw new DomainException("El tipo de movimiento es obligatorio");
        }
        if (channel == null) {
            throw new DomainException("El canal del movimiento es obligatorio");
        }
        Money value = requirePositive(money);
        validateChannelLinks(channel, cashRegisterId, bankAccountId);

        Instant resolvedWhen = when == null ? Instant.now() : when;
        Actor resolved = actor == null ? Actor.SYSTEM : actor;

        CashMovement movement = new CashMovement();
        movement.type = type;
        movement.amount = value.amount();
        movement.currencyCode = value.currency();
        movement.channel = channel;
        movement.cashRegisterId = cashRegisterId;
        movement.cashSessionId = cashSessionId;
        movement.bankAccountId = bankAccountId;
        movement.paymentMethod = paymentMethod;
        movement.concept = requireConcept(concept);
        movement.externalReference = blankToNull(externalReference, TreasuryPolicy.REFERENCE_MAX_LENGTH);
        movement.sourceDocumentType = blankToNull(sourceDocumentType, 64);
        movement.sourceDocumentId = sourceDocumentId;
        movement.status = CashMovementStatus.PROCESSED;
        movement.occurredAt = resolvedWhen;
        movement.movementDate = LocalDateTime.ofInstant(resolvedWhen, ZoneOffset.UTC);
        movement.createdAt = resolvedWhen;
        movement.createdBy = resolved.id();
        return movement;
    }

    /**
     * Ajuste de arqueo: el monto puede ser positivo (sobrante) o negativo (faltante).
     */
    public static CashMovement arqueoAdjustment(Money difference, Long cashRegisterId, Long cashSessionId,
                                                String note, Actor actor, Instant when) {
        if (difference == null || difference.isZero()) {
            throw new DomainException("El ajuste de arqueo exige una diferencia distinta de cero");
        }
        if (cashRegisterId == null || cashSessionId == null) {
            throw new DomainException("El ajuste de arqueo exige caja y sesión");
        }
        Money absolute = Money.of(difference.amount().abs(), difference.currency());
        CashMovement movement = record(
                CashMovementType.ADJUSTMENT,
                absolute,
                CashChannel.CASH_REGISTER,
                null,
                cashRegisterId,
                cashSessionId,
                null,
                note == null || note.isBlank() ? "Ajuste de arqueo" : note.trim(),
                null,
                TreasuryPolicy.SOURCE_SESSION_CLOSE,
                cashSessionId,
                actor,
                when
        );
        movement.amount = difference.amount();
        return movement;
    }

    public static CashMovement incomeFromCollection(Collection collection, Actor actor, Instant when) {
        if (collection == null) {
            throw new DomainException("La cobranza es obligatoria");
        }
        if (collection.getPaymentMethod() == null) {
            throw new DomainException("La cobranza exige método de pago");
        }
        if (collection.getAmount() == null || collection.getAmount() <= 0) {
            throw new DomainException("El monto del cobro debe ser positivo");
        }
        Money money = Money.of(BigDecimal.valueOf(collection.getAmount()), TreasuryPolicy.DEFAULT_CURRENCY);
        Instant resolved = when == null ? Instant.now() : when;
        CashMovement movement = record(
                CashMovementType.INCOME,
                money,
                CashChannel.CASH_REGISTER,
                collection.getPaymentMethod(),
                null,
                null,
                null,
                "Cobro venta " + (collection.getSale() == null ? "" : collection.getSale().getId()),
                collection.getReferenceNumber(),
                TreasuryPolicy.SOURCE_COLLECTION,
                collection.getId(),
                actor,
                resolved
        );
        movement.collection = collection;
        movement.sale = collection.getSale();
        return movement;
    }

    /**
     * Marca este movimiento como anulado y construye el movimiento compensatorio del libro.
     */
    public CashMovement voidWithCompensation(String reason, Actor actor, Instant when) {
        if (status == CashMovementStatus.ANNULLED) {
            throw new DomainException("El movimiento ya está anulado");
        }
        if (status != CashMovementStatus.PROCESSED) {
            throw new DomainException("Solo un movimiento procesado se puede anular");
        }
        Instant resolved = when == null ? Instant.now() : when;
        Actor resolvedActor = actor == null ? Actor.SYSTEM : actor;
        this.status = CashMovementStatus.ANNULLED;

        CashMovement compensation = record(
                CashMovementType.COMPENSATION,
                money(),
                channel,
                paymentMethod,
                cashRegisterId,
                cashSessionId,
                bankAccountId,
                reason == null || reason.isBlank()
                        ? "Anulación movimiento " + id
                        : reason.trim(),
                externalReference,
                "REVERSE_OF:" + this.type.name(),
                this.id,
                resolvedActor,
                resolved
        );
        compensation.compensatesMovementId = this.id;
        compensation.collection = this.collection;
        compensation.sale = this.sale;
        compensation.createdByUserId = this.createdByUserId;
        return compensation;
    }

    public void attachCreatedByUserId(Long userId) {
        this.createdByUserId = userId;
    }

    public Money money() {
        return Money.of(amount, currencyCode);
    }

    /**
     * Efecto neto sobre el saldo de caja de la sesión (ingresos positivos, egresos negativos).
     * Los movimientos {@link CashMovementStatus#ANNULLED} no cuentan; la compensación sí.
     */
    public Money signedEffectOnCash() {
        if (status != CashMovementStatus.PROCESSED) {
            return Money.zero(currencyCode);
        }
        Money value = money();
        return switch (type) {
            case INCOME, TRANSFER_IN -> value;
            case EXPENSE, TRANSFER_OUT -> Money.of(value.amount().negate(), currencyCode);
            case ADJUSTMENT -> value;
            case COMPENSATION -> compensationEffect(value);
        };
    }

    /**
     * Compensación: invierte el efecto del tipo original guardado en
     * {@code sourceDocumentType = REVERSE_OF:&lt;TYPE&gt;}.
     */
    private Money compensationEffect(Money value) {
        if (sourceDocumentType != null && sourceDocumentType.startsWith("REVERSE_OF:")) {
            CashMovementType originalType = CashMovementType.require(
                    sourceDocumentType.substring("REVERSE_OF:".length()));
            Money originalEffect = switch (originalType) {
                case INCOME, TRANSFER_IN, ADJUSTMENT -> value;
                case EXPENSE, TRANSFER_OUT, COMPENSATION -> Money.of(value.amount().negate(), currencyCode);
            };
            return Money.of(originalEffect.amount().negate(), currencyCode);
        }
        return Money.of(value.amount().negate(), currencyCode);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public CashMovementType getType() {
        return type;
    }

    /** Compatibilidad con DTOs/legacy que leían Double. */
    public Double getAmount() {
        return amount == null ? null : amount.doubleValue();
    }

    public BigDecimal getAmountValue() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public CashChannel getChannel() {
        return channel;
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

    public PaymentMethodConfig getPaymentMethod() {
        return paymentMethod;
    }

    public Collection getCollection() {
        return collection;
    }

    public Sale getSale() {
        return sale;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public CashMovementStatus getStatus() {
        return status;
    }

    public String getConcept() {
        return concept;
    }

    public String getSourceDocumentType() {
        return sourceDocumentType;
    }

    public Long getSourceDocumentId() {
        return sourceDocumentId;
    }

    public Long getCompensatesMovementId() {
        return compensatesMovementId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static void validateChannelLinks(CashChannel channel, Long cashRegisterId, Long bankAccountId) {
        if (channel == CashChannel.CASH_REGISTER && cashRegisterId == null) {
            // Permitido en cobros legacy sin sesión; el caso de uso de caja sí exige caja.
            return;
        }
        if (channel == CashChannel.BANK && bankAccountId == null) {
            throw new DomainException("Un movimiento bancario exige bankAccountId");
        }
    }

    private static Money requirePositive(Money money) {
        if (money == null) {
            throw new DomainException("El monto del movimiento es obligatorio");
        }
        if (money.amount().signum() <= 0) {
            throw new DomainException("El monto del movimiento debe ser positivo");
        }
        return money;
    }

    private static String requireConcept(String concept) {
        if (concept == null || concept.isBlank()) {
            throw new DomainException("El concepto del movimiento es obligatorio");
        }
        String trimmed = concept.trim();
        if (trimmed.length() > TreasuryPolicy.CONCEPT_MAX_LENGTH) {
            throw new DomainException("El concepto del movimiento excede la longitud permitida");
        }
        return trimmed;
    }

    private static String blankToNull(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new DomainException("El texto excede la longitud permitida");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CashMovement movement)) {
            return false;
        }
        return id != null && id.equals(movement.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
