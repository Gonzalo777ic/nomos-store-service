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
    @Enumerated(EnumType.STRING)
    private SaleStatus status;

    @Column(name = "total_amount", nullable = false)
    private java.math.BigDecimal totalAmount;

    @Column(name = "total_discount", nullable = false)
    private java.math.BigDecimal totalDiscount;

    @Column(name = "global_discount", nullable = false)
    @Builder.Default
    private java.math.BigDecimal globalDiscount = java.math.BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false)
    @Builder.Default
    private java.math.BigDecimal taxAmount = java.math.BigDecimal.ZERO;

    @Column(name = "base_amount", nullable = false)
    @Builder.Default
    private java.math.BigDecimal baseAmount = java.math.BigDecimal.ZERO;

    @Column(name = "sales_team_id")
    private Long salesTeamId;

    @Column(name = "territory_id")
    private Long territoryId;

    @Column(name = "commission_rule_id")
    private Long commissionRuleId;

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


    public java.math.BigDecimal getPaidAmount() {
        return accountsReceivable != null
                ? accountsReceivable.getPaidAmount()
                : java.math.BigDecimal.ZERO;
    }

    public java.math.BigDecimal getBalance() {
        if (accountsReceivable != null) {
            return accountsReceivable.getBalance();
        }
        return totalAmount != null ? totalAmount : java.math.BigDecimal.ZERO;
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

    public java.math.BigDecimal getTotalReturnedAmount() {
        if (returns == null) {
            return java.math.BigDecimal.ZERO;
        }
        return returns.stream()
                .filter(r -> r.getStatus() == SaleReturnStatus.CONFIRMED)
                .map(SaleReturn::getTotalRefundAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}