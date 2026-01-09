package com.nomos.store.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sale_id", nullable = false)
    private Long saleId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;
    @Column(name = "tax_rate_id", nullable = true)
    private Long taxRateId;
    @Column(name = "promotion_id", nullable = true)
    private Long promotionId;
}