package com.nomos.store.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cash_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime movementDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashMovementType type;

    @Column(nullable = false)
    private Double amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodConfig paymentMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Column(name = "external_reference")
    private String externalReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashMovementStatus status;

    private String concept;

    private Long createdByUserId;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.movementDate == null) {
            this.movementDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = CashMovementStatus.PROCESSED;
        }
    }
}