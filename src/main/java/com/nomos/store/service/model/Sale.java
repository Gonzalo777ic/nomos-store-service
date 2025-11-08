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

    // FK a Client (Puede ser nulo si es una venta anónima o 'mostrador')
    @Column(name = "client_id", nullable = true)
    private Long clientId;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    // Tipo de documento: Boleta, Factura, etc.
    @Enumerated(EnumType.STRING) // Le dice a JPA que lo guarde como String en la DB
    @Column(name = "type", nullable = false, length = 50)
    private SaleTypeEnum type;

    // Monto total de la venta (incluye impuestos después de descuentos)
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    // Descuento total aplicado en la transacción
    @Column(name = "total_discount", nullable = false)
    private Double totalDiscount;

    // Estado de la venta: COMPLETADA, CANCELADA, PENDIENTE (ej. para pagos a crédito)
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    // FK a User (Vendedor que realiza la transacción)
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // NOTA: Los detalles de la venta (productos) irían en una entidad SaleDetail
    // mapeada con @OneToMany.
}