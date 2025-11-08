package com.nomos.store.service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "payment_method_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del método (Ej: VISA, Yape, Efectivo, Transferencia BCP)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Tipo de clasificación (Ej: TARJETA, EFECTIVO, ELECTRÓNICO)
    @Column(name = "type", nullable = false)
    private String type;

    // NOTA: Se ha eliminado el campo 'feeRate' para simplificar.
}