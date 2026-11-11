package com.valeerp.crm;

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
 * Oportunidad del pipeline. Documento con estados; cierre por métodos de dominio, no delete.
 */
@Entity
@Table(name = "crm_opportunities")
public class CrmOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "pipeline_id", nullable = false)
    private Long pipelineId;

    @Column(name = "stage_id", nullable = false)
    private Long stageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OpportunityStatus status;

    @Column(name = "expected_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 1000)
    private String notes;

    @Column(name = "lost_reason", length = 400)
    private String lostReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected CrmOpportunity() {
    }

    public static CrmOpportunity open(String name, Long partnerId, Long leadId, Long pipelineId,
                                      CrmPipelineStage stage, Money expectedRevenue, String notes,
                                      Instant createdAt) {
        if (partnerId == null) {
            throw new DomainException("La oportunidad exige un partnerId");
        }
        if (pipelineId == null) {
            throw new DomainException("La oportunidad exige un pipeline");
        }
        if (stage == null) {
            throw new DomainException("La oportunidad exige una etapa");
        }
        if (!pipelineId.equals(stage.getPipelineId())) {
            throw new DomainException("La etapa no pertenece al pipeline indicado");
        }
        if (stage.isRetired()) {
            throw new DomainException("La etapa está dada de baja");
        }
        if (stage.getOutcome().isTerminal()) {
            throw new DomainException("Una oportunidad nueva no puede nacer en etapa terminal");
        }
        Money revenue = requireNonNegative(expectedRevenue);

        CrmOpportunity opportunity = new CrmOpportunity();
        opportunity.applyName(name);
        opportunity.partnerId = partnerId;
        opportunity.leadId = leadId;
        opportunity.pipelineId = pipelineId;
        opportunity.stageId = stage.getId();
        opportunity.status = OpportunityStatus.OPEN;
        opportunity.expectedAmount = revenue.amount();
        opportunity.currency = revenue.currency();
        opportunity.notes = blankToNull(notes);
        opportunity.createdAt = createdAt;
        return opportunity;
    }

    public void rename(String name, String notes) {
        assertOpen();
        applyName(name);
        this.notes = blankToNull(notes);
    }

    public void changeExpectedRevenue(Money expectedRevenue) {
        assertOpen();
        Money revenue = requireNonNegative(expectedRevenue);
        this.expectedAmount = revenue.amount();
        this.currency = revenue.currency();
    }

    public void moveToStage(CrmPipelineStage stage, Instant when) {
        assertOpen();
        if (stage == null) {
            throw new DomainException("La etapa es obligatoria");
        }
        if (!pipelineId.equals(stage.getPipelineId())) {
            throw new DomainException("La etapa no pertenece al pipeline de la oportunidad");
        }
        if (stage.isRetired()) {
            throw new DomainException("La etapa está dada de baja");
        }
        if (stage.getOutcome() == StageOutcome.LOST) {
            throw new DomainException("Para etapa perdida use el cierre con motivo");
        }
        this.stageId = stage.getId();
        applyOutcome(stage.getOutcome(), null, when);
    }

    public void win(CrmPipelineStage wonStage, Instant when) {
        assertOpen();
        if (wonStage == null || wonStage.getOutcome() != StageOutcome.WON) {
            throw new DomainException("Se requiere una etapa de ganado configurada");
        }
        if (!pipelineId.equals(wonStage.getPipelineId()) || wonStage.isRetired()) {
            throw new DomainException("La etapa de ganado no es válida para este pipeline");
        }
        this.stageId = wonStage.getId();
        applyOutcome(StageOutcome.WON, null, when);
    }

    public void lose(CrmPipelineStage lostStage, String reason, Instant when) {
        assertOpen();
        if (lostStage == null || lostStage.getOutcome() != StageOutcome.LOST) {
            throw new DomainException("Se requiere una etapa de perdido configurada");
        }
        if (!pipelineId.equals(lostStage.getPipelineId()) || lostStage.isRetired()) {
            throw new DomainException("La etapa de perdido no es válida para este pipeline");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("El motivo de pérdida es obligatorio");
        }
        this.stageId = lostStage.getId();
        applyOutcome(StageOutcome.LOST, reason.trim(), when);
    }

    public void cancel(Instant when) {
        assertOpen();
        this.status = OpportunityStatus.CANCELLED;
        this.closedAt = when;
    }

    public Money expectedRevenue() {
        return Money.of(expectedAmount, currency);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public Long getStageId() {
        return stageId;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNotes() {
        return notes;
    }

    public String getLostReason() {
        return lostReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    private void applyOutcome(StageOutcome outcome, String lostReason, Instant when) {
        if (outcome == StageOutcome.WON) {
            this.status = OpportunityStatus.WON;
            this.closedAt = when;
            this.lostReason = null;
        } else if (outcome == StageOutcome.LOST) {
            this.status = OpportunityStatus.LOST;
            this.closedAt = when;
            this.lostReason = lostReason;
        }
    }

    private void assertOpen() {
        if (!status.isOpen()) {
            throw new DomainException("La oportunidad ya está cerrada (" + status.name() + ")");
        }
    }

    private void applyName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la oportunidad es obligatorio");
        }
        this.name = name.trim();
    }

    private static Money requireNonNegative(Money money) {
        if (money == null) {
            throw new DomainException("El ingreso esperado es obligatorio");
        }
        if (money.isNegative()) {
            throw new DomainException("El ingreso esperado no puede ser negativo");
        }
        return money;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CrmOpportunity opportunity)) {
            return false;
        }
        return id != null && id.equals(opportunity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
