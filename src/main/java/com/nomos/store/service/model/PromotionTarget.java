package com.nomos.store.service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "promotion_targets", uniqueConstraints = {
        // Asegura que una promoción no tenga el mismo target dos veces
        @UniqueConstraint(columnNames = {"promotion_id", "target_type", "target_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK a la promoción a la que pertenece esta regla de alcance
    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    // Tipo de objetivo (PRODUCT o CATEGORY)
    @Column(name = "target_type", nullable = false)
    private String targetType;

    // ID del Producto o de la Categoría afectada
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // NOTA: No es necesario mapear la relación OneToMany aquí,
    // ya que la promoción es la dueña de la relación.
}