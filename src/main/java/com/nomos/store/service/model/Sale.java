package com.nomos.store.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "client_id", nullable = true)
    private Long clientId;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private SaleTypeEnum type;
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    @Column(name = "total_discount", nullable = false)
    private Double totalDiscount;
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
}