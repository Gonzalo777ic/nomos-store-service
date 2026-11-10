package com.valeerp.order;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerOrderStatus status;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerOrderLine> lines = new ArrayList<>();

    public static CustomerOrder open(Long customerId, String currency) {
        CustomerOrder order = new CustomerOrder();
        order.customerId = customerId;
        order.currency = currency;
        order.status = CustomerOrderStatus.RECIBIDO;
        order.total = BigDecimal.ZERO;
        order.createdAt = Instant.now();
        return order;
    }

    public void addLine(Long productId, String productName, int quantity, Money unitPrice) {
        if (status != CustomerOrderStatus.RECIBIDO) {
            throw new DomainException("El pedido ya no admite líneas");
        }
        CustomerOrderLine line = new CustomerOrderLine();
        line.attach(this, productId, productName, quantity, unitPrice);
        lines.add(line);
        total = lines.stream().map(CustomerOrderLine::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (lines.isEmpty()) {
            throw new DomainException("El pedido no tiene líneas");
        }
        this.status = CustomerOrderStatus.CONFIRMADO;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public CustomerOrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<CustomerOrderLine> getLines() {
        return lines;
    }
}
