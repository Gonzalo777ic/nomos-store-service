package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sale_return_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleReturnDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_return_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private SaleReturn saleReturn;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_sale_detail_id", nullable = false)
    @JsonIgnoreProperties({"sale", "hibernateLazyInitializer", "handler"})
    private SaleDetail originalSaleDetail;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Double subtotal;
}