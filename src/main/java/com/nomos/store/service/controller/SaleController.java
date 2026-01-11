package com.nomos.store.service.controller;

import com.nomos.store.service.model.PaymentConditionEnum;
import com.nomos.store.service.model.Sale;
import com.nomos.store.service.model.SaleDetail;
import com.nomos.store.service.model.SaleTypeEnum;
import com.nomos.store.service.repository.SaleDetailRepository;
import com.nomos.store.service.repository.SaleRepository;
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
        private String paymentCondition;

        private Integer creditDays;
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

    @GetMapping("/payment-conditions")
    public ResponseEntity<List<ReferenceDTO>> getPaymentConditions() {
        return ResponseEntity.ok(
                Arrays.stream(PaymentConditionEnum.values())
                        .map(pc -> new ReferenceDTO(pc.name(), pc.getDescription()))
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> createSale(@RequestBody SaleCreationRequest request) {
        log.info("Intentando crear venta para Cliente ID: {}", request.getClientId());

        try {
            if (request.getSellerId() == null || request.getSaleDate() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios (Vendedor o Fecha)");
            }
            if (request.getDetails() == null || request.getDetails().isEmpty()) {
                return ResponseEntity.badRequest().body("La venta debe tener al menos un detalle");
            }

            SaleTypeEnum saleType;
            try {
                saleType = SaleTypeEnum.valueOf(request.getType().toUpperCase());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Tipo de comprobante inválido: " + request.getType());
            }

            PaymentConditionEnum paymentCondition;
            try {
                if (request.getPaymentCondition() == null) {
                    return ResponseEntity.badRequest().body("La condición de pago es obligatoria (CONTADO/CREDITO)");
                }
                paymentCondition = PaymentConditionEnum.valueOf(request.getPaymentCondition().toUpperCase());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Condición de pago inválida: " + request.getPaymentCondition());
            }

            LocalDateTime calculatedDueDate = request.getSaleDate();
            Integer actualCreditDays = 0;

            if (paymentCondition == PaymentConditionEnum.CREDITO) {
                actualCreditDays = (request.getCreditDays() != null && request.getCreditDays() > 0)
                        ? request.getCreditDays()
                        : 30;
                calculatedDueDate = request.getSaleDate().plusDays(actualCreditDays);
            } else {
                actualCreditDays = 0;
            }

            double totalAmount = request.getDetails().stream()
                    .mapToDouble(SaleRequestDetail::getSubtotal)
                    .sum();

            Sale newSale = Sale.builder()
                    .clientId(request.getClientId())
                    .saleDate(request.getSaleDate())
                    .type(saleType)
                    .paymentCondition(paymentCondition)
                    .sellerId(request.getSellerId())
                    .totalAmount(totalAmount)
                    .totalDiscount(0.0)
                    .status("EMITIDA")
                    .dueDate(calculatedDueDate)
                    .creditDays(actualCreditDays)
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


    @PatchMapping("/{id}/cancel")
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> cancelSale(@PathVariable Long id) {
        return saleRepository.findById(id).map(sale -> {

            if ("CANCELADA".equals(sale.getStatus())) {
                return ResponseEntity.badRequest().body("La venta ya está cancelada.");
            }

            sale.setStatus("CANCELADA");

            if (sale.getCollections() != null && !sale.getCollections().isEmpty()) {
                sale.getCollections().forEach(collection -> {

                    if (!"ANULADO".equals(collection.getStatus())) {
                        collection.setStatus("ANULADO");
                        log.info("Cobranza ID {} anulada por cancelación de venta padre.", collection.getId());
                    }
                });
            }


            saleRepository.save(sale);

            log.info("Venta ID {} y sus cobros han sido cancelados.", id);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}

