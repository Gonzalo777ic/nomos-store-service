package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Equipo de venta. Maestro con soft-delete.
 */
@Entity
@Table(name = "sales_teams")
public class SalesTeam extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "leader_user_id")
    private Long leaderUserId;

    @Column(name = "territory_id")
    private Long territoryId;

    @Column(length = 240)
    private String description;

    protected SalesTeam() {
    }

    public static SalesTeam create(String code, String name, Long leaderUserId, Long territoryId, String description) {
        SalesTeam team = new SalesTeam();
        team.rename(code, name, leaderUserId, territoryId, description);
        return team;
    }

    public void rename(String code, String name, Long leaderUserId, Long territoryId, String description) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código del equipo es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del equipo es obligatorio");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.leaderUserId = leaderUserId;
        this.territoryId = territoryId;
        this.description = blankToNull(description);
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

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public Long getTerritoryId() {
        return territoryId;
    }

    public String getDescription() {
        return description;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SalesTeam team)) {
            return false;
        }
        return id != null && id.equals(team.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
