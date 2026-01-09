package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // TIPO DE DOCUMENTO (Boleta, Factura, Ticket)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private SaleTypeEnum type;

    // CONDICIÓN DE PAGO (Contado, Crédito)
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
    private List<SaleDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sale")
    @Builder.Default
    private List<Collection> collections = new ArrayList<>();

    // -- CAMPOS PARA CRÉDITO --
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "credit_days")
    private Integer creditDays;

    /**
     * Calcula el monto total pagado sumando las cobranzas registradas.
     * @return suma de pagos o 0.0
     */
    public Double getPaidAmount() {
        if (collections == null || collections.isEmpty()) {
            return 0.0;
        }
        return collections.stream()
                .filter(c -> c.getAmount() != null)
                .mapToDouble(Collection::getAmount)
                .sum();
    }

    /**
     * Calcula el saldo pendiente por cobrar.
     * @return totalAmount - paidAmount
     */
    public Double getBalance() {
        if (totalAmount == null) return 0.0;
        return this.totalAmount - getPaidAmount();
    }

    /**
     * Verifica si la venta está totalmente pagada.
     */
    public boolean isFullyPaid() {
        return getBalance() <= 0.01;
    }

    public boolean isOverdue() {
        // Si el saldo es casi 0 (pagado), no está vencida
        if (getBalance() <= 0.01) return false;

        // Si hoy es mayor que la fecha de vencimiento, está vencida
        return LocalDateTime.now().isAfter(dueDate);
    }

    public String getPaymentStatus() {
        if (getBalance() <= 0.01) return "PAGADO";
        if (isOverdue()) return "VENCIDO";
        return "PENDIENTE";
    }
}