package com.nomos.store.service.controller;

import com.nomos.store.service.model.Sale;
import com.nomos.store.service.model.SaleDetail;
import com.nomos.store.service.model.SaleTypeEnum;
import com.nomos.store.service.repository.SaleRepository;
import com.nomos.store.service.repository.SaleDetailRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SaleController {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    @Data
    public static class SaleRequestDetail {
        private Long productId;
        private Double unitPrice;
        private Integer quantity;
        private Double subtotal;
        private Long taxRateId;
        private Long promotionId;
    }

    @Data
    public static class SaleCreationRequest {
        private Long clientId;
        private LocalDateTime saleDate;
        private String type;
        private Long sellerId;
        private List<SaleRequestDetail> details;
    }

    @Data
    public static class ReferenceDTO {
        private final String key;
        private final String description;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER', 'ROLE_VENDOR')")
    public ResponseEntity<List<Sale>> getAllSales() {
        log.info("Consultando todas las ventas...");
        return ResponseEntity.ok(saleRepository.findAll());
    }

    @GetMapping("/types")
    public ResponseEntity<List<ReferenceDTO>> getSaleTypes() {
        return ResponseEntity.ok(
                Arrays.stream(SaleTypeEnum.values())
                        .map(st -> new ReferenceDTO(st.name(), st.getDescription()))
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> createSale(@RequestBody SaleCreationRequest request) {
        log.info("Intentando crear venta para Cliente ID: {}", request.getClientId());

        try {
            SaleTypeEnum saleType;
            try {
                saleType = SaleTypeEnum.valueOf(request.getType().toUpperCase());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Tipo de venta inválido: " + request.getType());
            }
            if (request.getSellerId() == null || request.getSaleDate() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios (Vendedor o Fecha)");
            }
            if (request.getDetails() == null || request.getDetails().isEmpty()) {
                return ResponseEntity.badRequest().body("La venta debe tener al menos un detalle");
            }
            double totalAmount = request.getDetails().stream()
                    .mapToDouble(SaleRequestDetail::getSubtotal)
                    .sum();

            Sale newSale = Sale.builder()
                    .clientId(request.getClientId())
                    .saleDate(request.getSaleDate())
                    .type(saleType)
                    .sellerId(request.getSellerId())
                    .totalAmount(totalAmount)
                    .totalDiscount(0.0)
                    .status("COMPLETADA")
                    .build();

            Sale savedSale = saleRepository.save(newSale);
            for (SaleRequestDetail d : request.getDetails()) {
                if (d.getTaxRateId() == null) {
                    throw new IllegalArgumentException("El producto " + d.getProductId() + " no tiene tasa de impuesto");
                }

                SaleDetail detail = new SaleDetail();
                detail.setSale(savedSale);

                detail.setProductId(d.getProductId());
                detail.setUnitPrice(d.getUnitPrice());
                detail.setQuantity(d.getQuantity());
                detail.setSubtotal(d.getSubtotal());
                detail.setTaxRateId(d.getTaxRateId());
                detail.setPromotionId(d.getPromotionId());

                saleDetailRepository.save(detail);
            }

            log.info("Venta creada exitosamente con ID: {}", savedSale.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSale);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al crear venta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la venta");
        }
    }
}