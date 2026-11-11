package com.valeerp.crm;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Pipeline comercial configurable. Maestro con soft-delete; etapas hijas se archivan con la raíz.
 */
@Entity
@Table(name = "crm_pipelines")
public class CrmPipeline extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 400)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL)
    @OrderBy("sequenceNo ASC")
    private List<CrmPipelineStage> stages = new ArrayList<>();

    protected CrmPipeline() {
    }

    public static CrmPipeline open(String code, String name, String description, Instant createdAt) {
        CrmPipeline pipeline = new CrmPipeline();
        pipeline.applyIdentity(code, name, description);
        pipeline.createdAt = createdAt;
        return pipeline;
    }

    public void rename(String code, String name, String description) {
        applyIdentity(code, name, description);
    }

    public CrmPipelineStage addStage(String code, String name, Integer sequenceNo, StageOutcome outcome) {
        String normalized = normalizeCode(code);
        boolean exists = activeStages().stream().anyMatch(stage -> stage.sameCode(normalized));
        if (exists) {
            throw new DomainException("Ya existe una etapa con código " + normalized);
        }
        int sequence = sequenceNo == null ? nextSequence() : sequenceNo;
        CrmPipelineStage stage = CrmPipelineStage.create(this, normalized, name, sequence, outcome);
        stages.add(stage);
        return stage;
    }

    public void updateStage(Long stageId, String code, String name, Integer sequenceNo, StageOutcome outcome) {
        CrmPipelineStage stage = requireActiveStage(stageId);
        String normalized = normalizeCode(code);
        boolean conflict = activeStages().stream()
                .filter(other -> !other.getId().equals(stageId))
                .anyMatch(other -> other.sameCode(normalized));
        if (conflict) {
            throw new DomainException("Ya existe una etapa con código " + normalized);
        }
        int sequence = sequenceNo == null ? stage.getSequenceNo() : sequenceNo;
        stage.rename(normalized, name, sequence, outcome);
    }

    public void retireStage(Long stageId, Actor actor, Instant when) {
        CrmPipelineStage stage = requireActiveStage(stageId);
        stage.retire(actor, when);
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
        for (CrmPipelineStage stage : stages) {
            if (!stage.isRetired()) {
                stage.retire(actor, when);
            }
        }
    }

    public CrmPipelineStage requireActiveStage(Long stageId) {
        if (stageId == null) {
            throw new DomainException("La etapa es obligatoria");
        }
        return activeStages().stream()
                .filter(stage -> stageId.equals(stage.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("NOT_FOUND", "Etapa no encontrada"));
    }

    public CrmPipelineStage entryStage() {
        return activeStages().stream()
                .filter(stage -> stage.getOutcome() == StageOutcome.OPEN)
                .min(Comparator.comparingInt(CrmPipelineStage::getSequenceNo))
                .orElseThrow(() -> new DomainException("El pipeline no tiene etapa de entrada abierta"));
    }

    public CrmPipelineStage findWonStage() {
        return activeStages().stream()
                .filter(stage -> stage.getOutcome() == StageOutcome.WON)
                .min(Comparator.comparingInt(CrmPipelineStage::getSequenceNo))
                .orElseThrow(() -> new DomainException("El pipeline no tiene etapa de ganado configurada"));
    }

    public CrmPipelineStage findLostStage() {
        return activeStages().stream()
                .filter(stage -> stage.getOutcome() == StageOutcome.LOST)
                .min(Comparator.comparingInt(CrmPipelineStage::getSequenceNo))
                .orElseThrow(() -> new DomainException("El pipeline no tiene etapa de perdido configurada"));
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<CrmPipelineStage> getStages() {
        return Collections.unmodifiableList(stages);
    }

    public List<CrmPipelineStage> activeStages() {
        return stages.stream()
                .filter(stage -> !stage.isRetired())
                .sorted(Comparator.comparingInt(CrmPipelineStage::getSequenceNo))
                .toList();
    }

    private int nextSequence() {
        return activeStages().stream()
                .mapToInt(CrmPipelineStage::getSequenceNo)
                .max()
                .orElse(0) + CrmPolicy.DEFAULT_STAGE_SEQUENCE_STEP;
    }

    private void applyIdentity(String code, String name, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de pipeline es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de pipeline es obligatorio");
        }
        this.code = normalizeCode(code);
        this.name = name.trim();
        this.description = blankToNull(description);
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CrmPipeline pipeline)) {
            return false;
        }
        return id != null && id.equals(pipeline.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
