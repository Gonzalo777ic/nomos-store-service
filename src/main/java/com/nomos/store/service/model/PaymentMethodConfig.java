package com.nomos.store.service.model;

import com.valeerp.kernel.SoftDeletable;
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
public class PaymentMethodConfig extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "type", nullable = false)
    private String type;
}