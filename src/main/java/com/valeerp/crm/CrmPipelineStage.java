package com.valeerp.crm;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
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

/**
 * Etapa configurable del pipeline. Maestro con baja lógica; el UI las consume vía API.
 */
@Entity
@Table(name = "crm_pipeline_stages")
public class CrmPipelineStage extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private CrmPipeline pipeline;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StageOutcome outcome;

    protected CrmPipelineStage() {
    }

    static CrmPipelineStage create(CrmPipeline pipeline, String code, String name,
                                   int sequenceNo, StageOutcome outcome) {
        if (pipeline == null) {
            throw new DomainException("La etapa exige un pipeline");
        }
        CrmPipelineStage stage = new CrmPipelineStage();
        stage.pipeline = pipeline;
        stage.apply(code, name, sequenceNo, outcome == null ? StageOutcome.OPEN : outcome);
        return stage;
    }

    void rename(String code, String name, int sequenceNo, StageOutcome outcome) {
        apply(code, name, sequenceNo, outcome == null ? this.outcome : outcome);
    }

    void changeSequence(int sequenceNo) {
        if (sequenceNo < 0) {
            throw new DomainException("La secuencia de etapa no puede ser negativa");
        }
        this.sequenceNo = sequenceNo;
    }

    public Long getId() {
        return id;
    }

    public Long getPipelineId() {
        return pipeline == null ? null : pipeline.getId();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public StageOutcome getOutcome() {
        return outcome;
    }

    boolean sameCode(String other) {
        return code != null && code.equalsIgnoreCase(other == null ? "" : other.trim());
    }

    private void apply(String code, String name, int sequenceNo, StageOutcome outcome) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de etapa es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de etapa es obligatorio");
        }
        if (sequenceNo < 0) {
            throw new DomainException("La secuencia de etapa no puede ser negativa");
        }
        if (outcome == null) {
            throw new DomainException("El resultado de etapa es obligatorio");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.sequenceNo = sequenceNo;
        this.outcome = outcome;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CrmPipelineStage stage)) {
            return false;
        }
        return id != null && id.equals(stage.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
