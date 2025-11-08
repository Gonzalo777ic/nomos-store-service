package com.nomos.store.service.controller;

import com.nomos.store.service.model.Sale;
import com.nomos.store.service.model.SaleDetail;
import com.nomos.store.service.model.SaleTypeEnum;
import com.nomos.store.service.repository.SaleRepository;
import com.nomos.store.service.repository.SaleDetailRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/store/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;


    // --- NUEVO MÉTODO AGREGADO ---
    /** 🔑 GET /api/store/sales - Obtener lista de todas las ventas */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER', 'ROLE_VENDOR')")
    public ResponseEntity<List<Sale>> getAllSales() {
        try {
            List<Sale> sales = saleRepository.findAll();
            System.out.println("Ventas encontradas: " + sales.size());
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // --- CLASES ANIDADAS QUE DEFINEN LA ESTRUCTURA DE LA PETICIÓN ---

    /** Estructura que viene en el JSON para el detalle */
    @Data
    public static class SaleRequestDetail {
        private Long productId;
        private Double unitPrice;
        private Integer quantity;
        private Double subtotal;
        private Long taxRateId;
        private Long promotionId;
    }

    /** 🔑 Estructura que viene en el JSON para la creación de la Venta completa */
    @Data
    public static class SaleCreationRequest {
        private Long clientId;
        private LocalDateTime saleDate;
        private String type; // Permite el String del frontend (ej: "BOLETA")
        private Long sellerId;
        private List<SaleRequestDetail> details;
    }

    /** DTO para exponer el Enum */
    @Data
    public static class ReferenceDTO {
        private final String key;
        private final String description;
    }
    // --- FIN DE CLASES ANIDADAS ---

    /** 🔑 POST /api/store/sales - Crear nueva venta (TODO en el Controller) */
    @PostMapping
    @Transactional // ¡MUY IMPORTANTE!
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Sale> createSale(@RequestBody SaleCreationRequest saleRequest) {

        // 1. Manejo del tipo de Venta (String a Enum)
        SaleTypeEnum saleType;
        try {
            // Convierte el String recibido (ej: "BOLETA") al Enum (SaleTypeEnum.BOLETA)
            saleType = SaleTypeEnum.valueOf(saleRequest.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Tipo de venta inválido: " + saleRequest.getType());
            // Si el String no corresponde a un Enum, devuelve 400 con un mensaje
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // 2. VALIDACIÓN BÁSICA DE CABECERA
            if (saleRequest.getSellerId() == null || saleRequest.getSaleDate() == null || saleRequest.getType() == null) {
                return ResponseEntity.badRequest().body(null); // Faltan campos NOT NULL
            }
            if (saleRequest.getDetails() == null || saleRequest.getDetails().isEmpty()) {
                return ResponseEntity.badRequest().body(null); // No hay detalles
            }

            // 3. CÁLCULO DE TOTALES y VALIDACIÓN DE DETALLES
            double totalAmount = 0.0;
            final double totalDiscount = 0.0;

            for (SaleRequestDetail detail : saleRequest.getDetails()) {
                // Validación estricta para campos NOT NULL en SaleDetail (como taxRateId)
                if (detail.getProductId() == null || detail.getQuantity() == null || detail.getQuantity() <= 0
                        || detail.getUnitPrice() == null || detail.getUnitPrice() < 0) {

                    return ResponseEntity.badRequest().body(null); // Detalle básico inválido
                }

                // 🔑 VERIFICACIÓN CLAVE: taxRateId (Debe ser NOT NULL y > 0)
                if (detail.getTaxRateId() == null || detail.getTaxRateId() <= 0) {
                    System.err.println("Error 400: taxRateId es nulo o <= 0 para el producto ID: " + detail.getProductId());
                    // Retorna un 400 si el impuesto es nulo (violando nullable=false en DB) o inválido
                    return ResponseEntity.badRequest().body(null);
                }

                totalAmount += detail.getSubtotal();
            }

            // 4. CREACIÓN Y GUARDADO DE LA CABECERA (Sale)
            Sale newSale = new Sale();
            newSale.setClientId(saleRequest.getClientId());
            newSale.setSaleDate(saleRequest.getSaleDate());
            newSale.setType(saleType); // ✅ ASIGNACIÓN CORRECTA DEL ENUM
            newSale.setSellerId(saleRequest.getSellerId());
            newSale.setTotalAmount(totalAmount);
            newSale.setTotalDiscount(totalDiscount);
            newSale.setStatus("COMPLETADA");

            Sale savedSale = saleRepository.save(newSale);

            // 5. CREACIÓN Y GUARDADO DE LOS DETALLES (SaleDetail)
            for (SaleRequestDetail detailPayload : saleRequest.getDetails()) {
                SaleDetail detail = new SaleDetail();
                detail.setSaleId(savedSale.getId());

                detail.setProductId(detailPayload.getProductId());
                detail.setUnitPrice(detailPayload.getUnitPrice());
                detail.setQuantity(detailPayload.getQuantity());
                detail.setSubtotal(detailPayload.getSubtotal());
                detail.setTaxRateId(detailPayload.getTaxRateId());
                // PromotionId es nullable=true, así que Long promotionId = null es válido si no se envía.
                detail.setPromotionId(detailPayload.getPromotionId());

                saleDetailRepository.save(detail);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedSale);

        } catch (Exception e) {
            System.err.println("Error FATAL en createSale: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /** * 🌐 GET /api/store/sales/types - Expone los valores del Enum */
    @GetMapping("/types")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public ResponseEntity<List<ReferenceDTO>> getSaleTypes() {
        return ResponseEntity.ok(
                Arrays.stream(SaleTypeEnum.values())
                        .map(st -> new ReferenceDTO(st.name(), st.getDescription()))
                        .collect(Collectors.toList())
        );
    }
}
