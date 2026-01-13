package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "collections")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties({"details", "accountsReceivable"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_receivable_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AccountsReceivable accountsReceivable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Installment installment;

    @Column(name = "collection_date", nullable = false)
    private LocalDateTime collectionDate;

    @Column(nullable = false)
    private Double amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodConfig paymentMethod;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "status", nullable = false)
    private String status;

    @PrePersist
    public void prePersist() {
        if (this.collectionDate == null) {
            this.collectionDate = LocalDateTime.now();
        }
    }
}