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

    // FK a Sale (Venta a la que pertenece este ítem)
    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    // FK a Product (Producto vendido)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Precio unitario al momento de la venta
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Subtotal = (unitPrice * quantity) - totalDiscountItem
    // Nota: El cálculo real de impuestos y descuentos se hará en el Service
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    // FK a TaxRate (Tasa de impuesto aplicada, ej. IGV/IVA)
    @Column(name = "tax_rate_id", nullable = true)
    private Long taxRateId;

    // FK a Promotion (Promoción aplicada, puede ser nulo)
    @Column(name = "promotion_id", nullable = true)
    private Long promotionId;

    // Opcional: Podrías añadir un campo para el descuento específico del ítem si la promoción es por ítem.
    // @Column(name = "item_discount", nullable = false)
    // private Double itemDiscount = 0.0;
}