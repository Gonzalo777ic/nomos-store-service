package com.nomos.store.service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Tipo de descuento (Ej: PORCENTAJE, MONTO_FIJO, TRES_POR_DOS)
    @Column(nullable = false)
    private String type;

    // Valor del descuento (Ej: 0.10 para 10%, 5.00 para $5)
    @Column(nullable = false)
    private Double discountValue;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Alcance de la promoción (Ej: PRODUCT, CATEGORY, SALE_TOTAL)
    @Column(nullable = false)
    private String appliesTo;

    // NOTA: La relación OneToMany a PromotionTarget se implementará al definir esa entidad.
}