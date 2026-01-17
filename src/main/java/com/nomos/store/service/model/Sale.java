package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private SaleTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_condition", nullable = false, length = 50)
    private PaymentConditionEnum paymentCondition;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "total_discount", nullable = false)
    private Double totalDiscount;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sale")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SaleDetail> details = new ArrayList<>();

    @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("sale")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AccountsReceivable accountsReceivable;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "credit_days")
    private Integer creditDays;


    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sale")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<SalesDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY)


    @JsonIgnoreProperties({"sale", "hibernateLazyInitializer", "handler"})
    private List<SaleReturn> returns = new ArrayList<>();


    public Double getPaidAmount() {
        return accountsReceivable != null ? accountsReceivable.getPaidAmount() : 0.0;
    }

    public Double getBalance() {
        return accountsReceivable != null ? accountsReceivable.getBalance() : (totalAmount != null ? totalAmount : 0.0);
    }

    public String getPaymentStatus() {
        if (accountsReceivable == null) return "PENDIENTE";
        return accountsReceivable.getStatus().name();
    }

    public boolean isOverdue() {
        if (accountsReceivable == null) return false;
        LocalDate today = LocalDate.now();

        if (accountsReceivable.getInstallments() == null) return false;

        return accountsReceivable.getInstallments().stream()
                .anyMatch(i -> i.isOverdue(today));
    }

    public boolean hasFiscalDocument() {
        if (documents == null || documents.isEmpty()) return false;
        return documents.stream()
                .anyMatch(d -> d.getStatus() != SalesDocumentStatus.VOIDED &&
                        d.getStatus() != SalesDocumentStatus.REJECTED);
    }

    public boolean hasReturns() {
        return returns != null && !returns.isEmpty() &&
                returns.stream().anyMatch(r -> r.getStatus() == SaleReturnStatus.CONFIRMED);
    }

    public Double getTotalReturnedAmount() {
        if (returns == null) return 0.0;
        return returns.stream()
                .filter(r -> r.getStatus() == SaleReturnStatus.CONFIRMED)
                .mapToDouble(SaleReturn::getTotalRefundAmount)
                .sum();
    }
}