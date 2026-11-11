package com.valeerp.accounting;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/**
 * Periodo contable. Libro de configuración operativa: el cierre bloquea diarios del rango.
 * No se elimina un periodo cerrado.
 */
@Entity
@Table(name = "accounting_periods")
public class AccountingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountingPeriodStatus status;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by", length = 120)
    private String closedBy;

    protected AccountingPeriod() {
    }

    public static AccountingPeriod open(Long companyId, String code, LocalDate startDate, LocalDate endDate) {
        if (companyId == null) {
            throw new DomainException("El periodo exige compañía");
        }
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de periodo es obligatorio");
        }
        if (startDate == null || endDate == null) {
            throw new DomainException("El periodo exige fechas de inicio y fin");
        }
        if (endDate.isBefore(startDate)) {
            throw new DomainException("La fecha fin no puede ser anterior al inicio");
        }
        AccountingPeriod period = new AccountingPeriod();
        period.companyId = companyId;
        period.code = code.trim().toUpperCase(Locale.ROOT);
        period.startDate = startDate;
        period.endDate = endDate;
        period.status = AccountingPeriodStatus.OPEN;
        return period;
    }

    /**
     * Cierra el periodo: a partir de aquí los diarios de la compañía no aceptan
     * publicación ni anulación de asientos con fecha en el rango.
     */
    public void close(Actor actor, Instant when) {
        if (status == AccountingPeriodStatus.CLOSED) {
            throw new DomainException("El periodo ya está cerrado");
        }
        Actor resolved = actor == null ? Actor.SYSTEM : actor;
        this.status = AccountingPeriodStatus.CLOSED;
        this.closedAt = when;
        this.closedBy = resolved.id();
    }

    public void assertOpenFor(LocalDate entryDate) {
        if (entryDate == null) {
            throw new DomainException("La fecha del asiento es obligatoria");
        }
        if (!contains(entryDate)) {
            return;
        }
        if (status == AccountingPeriodStatus.CLOSED) {
            throw new DomainException(
                    "Periodo contable cerrado (" + code + "); los diarios están bloqueados para " + entryDate);
        }
    }

    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean isClosed() {
        return status == AccountingPeriodStatus.CLOSED;
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public AccountingPeriodStatus getStatus() {
        return status;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public String getClosedBy() {
        return closedBy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AccountingPeriod period) || id == null) {
            return false;
        }
        return id.equals(period.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
