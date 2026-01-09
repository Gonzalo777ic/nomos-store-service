package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonIgnoreProperties("details") // Evita ciclos infinitos al serializar si Sale tiene lista de collections
    private Sale sale;

    @Column(name = "collection_date", nullable = false)
    private LocalDateTime collectionDate;

    @Column(nullable = false)
    private Double amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodConfig paymentMethod;

    @Column(name = "reference_number")
    private String referenceNumber;

    @PrePersist
    public void prePersist() {
        if (this.collectionDate == null) {
            this.collectionDate = LocalDateTime.now();
        }
    }
}