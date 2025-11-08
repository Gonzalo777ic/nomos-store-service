package com.nomos.store.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tax_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del impuesto (Ej: IGV General, IVA Reducido)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Tasa en formato decimal (Ej: 0.18 para 18%)
    @Column(name = "rate", nullable = false)
    private Double rate;

    // Se eliminan los campos startDate y endDate
}