package com.valeerp.helpdesk;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;

/**
 * Política SLA configurable. Maestro con soft-delete.
 */
@Entity
@Table(name = "helpdesk_slas")
public class HelpdeskSla extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 400)
    private String description;

    @Column(name = "response_hours", nullable = false)
    private int responseHours;

    @Column(name = "resolution_hours", nullable = false)
    private int resolutionHours;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HelpdeskSla() {
    }

    public static HelpdeskSla open(String code, String name, String description,
                                   Integer responseHours, Integer resolutionHours, Instant createdAt) {
        HelpdeskSla sla = new HelpdeskSla();
        sla.applyIdentity(code, name, description);
        sla.applyTargets(responseHours, resolutionHours);
        sla.createdAt = createdAt;
        return sla;
    }

    public void rename(String code, String name, String description,
                       Integer responseHours, Integer resolutionHours) {
        applyIdentity(code, name, description);
        applyTargets(responseHours, resolutionHours);
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
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

    public int getResponseHours() {
        return responseHours;
    }

    public int getResolutionHours() {
        return resolutionHours;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void applyIdentity(String code, String name, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de SLA es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de SLA es obligatorio");
        }
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.name = name.trim();
        this.description = blankToNull(description);
    }

    private void applyTargets(Integer responseHours, Integer resolutionHours) {
        int response = responseHours == null ? HelpdeskPolicy.DEFAULT_RESPONSE_HOURS : responseHours;
        int resolution = resolutionHours == null ? HelpdeskPolicy.DEFAULT_RESOLUTION_HOURS : resolutionHours;
        if (response < HelpdeskPolicy.MIN_SLA_HOURS) {
            throw new DomainException("Las horas de respuesta SLA deben ser al menos " + HelpdeskPolicy.MIN_SLA_HOURS);
        }
        if (resolution < HelpdeskPolicy.MIN_SLA_HOURS) {
            throw new DomainException("Las horas de resolución SLA deben ser al menos " + HelpdeskPolicy.MIN_SLA_HOURS);
        }
        if (resolution < response) {
            throw new DomainException("La resolución SLA no puede ser menor que la respuesta");
        }
        this.responseHours = response;
        this.resolutionHours = resolution;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HelpdeskSla sla)) {
            return false;
        }
        return id != null && id.equals(sla.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
