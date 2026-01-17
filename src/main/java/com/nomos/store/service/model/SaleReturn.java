package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties({"returns", "details", "documents", "hibernateLazyInitializer", "handler"})
    private Sale sale;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleReturnType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleReturnStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "total_refund_amount", nullable = false)
    private Double totalRefundAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_document_id")
    @JsonIgnoreProperties({"sale", "documents", "hibernateLazyInitializer", "handler"})
    private SalesDocument creditNote;

    @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SaleReturnDetail> details = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdByUserId;
}