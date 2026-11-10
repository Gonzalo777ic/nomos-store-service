package com.valeerp.kernel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeletable {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 120)
    private String deletedBy;

    public void retire(String actor, Instant when) {
        if (this.deletedAt != null) {
            throw new DomainException("El registro ya fue dado de baja");
        }
        this.deletedAt = when;
        this.deletedBy = actor == null || actor.isBlank() ? "sistema" : actor;
    }

    @JsonIgnore
    public boolean isRetired() {
        return deletedAt != null;
    }

    @JsonIgnore
    public Instant getDeletedAt() {
        return deletedAt;
    }

    @JsonIgnore
    public String getDeletedBy() {
        return deletedBy;
    }
}
