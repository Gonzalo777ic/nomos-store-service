package com.nomos.store.service.controller;

import com.nomos.store.service.model.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        private Integer numberOfInstallments;
    }

    @Data
    public static class ReferenceDTO {
        private final String key;
        private final String description;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER', 'ROLE_VENDOR')")
    public ResponseEntity<List<Sale>> getAllSales() {
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
        log.info("Creando venta y estructura financiera para Cliente ID: {}", request.getClientId());

        try {

            if (request.getSellerId() == null || request.getSaleDate() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios (Vendedor o Fecha)");
            }
            if (request.getDetails() == null || request.getDetails().isEmpty()) {
                return ResponseEntity.badRequest().body("La venta debe tener al menos un detalle");
            }

            SaleTypeEnum saleType = SaleTypeEnum.valueOf(request.getType().toUpperCase());
            PaymentConditionEnum paymentCondition = PaymentConditionEnum.valueOf(request.getPaymentCondition().toUpperCase());

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
                    .dueDate(request.getSaleDate().plusDays(paymentCondition == PaymentConditionEnum.CREDITO ? (request.getCreditDays() != null ? request.getCreditDays() : 0) : 0))
                    .creditDays(request.getCreditDays())
                    .build();


            Sale savedSale = saleRepository.save(newSale);

            AccountsReceivable ar = AccountsReceivable.builder()
                    .sale(savedSale) // Ahora savedSale tiene ID real
                    .totalAmount(totalAmount)
                    .status(AccountsReceivableStatus.ACTIVE)
                    .build();

            Integer installmentsCount = request.getNumberOfInstallments();
            List<Installment> installments = generateInstallments(
                    ar,
                    paymentCondition,
                    installmentsCount,
                    request.getSaleDate().toLocalDate(),
                    totalAmount
            );
            ar.setInstallments(installments);







            savedSale.setAccountsReceivable(ar);
            savedSale = saleRepository.save(savedSale); // Re-guardar para activar cascade y actualizar referencia

            for (SaleRequestDetail d : request.getDetails()) {
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

            log.info("Venta #{} registrada exitosamente.", savedSale.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSale);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creando venta", e);

            throw new RuntimeException("Error interno al crear venta: " + e.getMessage());
        }
    }

    private List<Installment> generateInstallments(AccountsReceivable ar, PaymentConditionEnum condition, Integer numberOfInstallments, LocalDate baseDate, Double totalAmount) {
        List<Installment> installments = new ArrayList<>();

        if (condition == PaymentConditionEnum.CONTADO) {

            installments.add(Installment.builder()
                    .accountsReceivable(ar)
                    .number(1)
                    .expectedAmount(totalAmount)
                    .dueDate(baseDate)
                    .status(InstallmentStatus.PENDING) // El frontend maneja la lógica de visualización
                    .build());
        } else {

            int n = (numberOfInstallments != null && numberOfInstallments > 0) ? numberOfInstallments : 1;


            double rawAmount = totalAmount / n;
            double roundedAmount = Math.round(rawAmount * 100.0) / 100.0; // 2 decimales

            double accumulated = 0.0;

            for (int i = 1; i <= n; i++) {
                double currentAmount;

                if (i == n) {
                    currentAmount = Math.round((totalAmount - accumulated) * 100.0) / 100.0;
                } else {
                    currentAmount = roundedAmount;
                }

                accumulated += currentAmount;

                installments.add(Installment.builder()
                        .accountsReceivable(ar)
                        .number(i)
                        .expectedAmount(currentAmount)
                        .dueDate(baseDate.plusMonths(i)) // Frecuencia Mensual
                        .status(InstallmentStatus.PENDING)
                        .build());
            }
        }
        return installments;
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

            if (sale.getAccountsReceivable() != null) {

                sale.getAccountsReceivable().setStatus(AccountsReceivableStatus.CANCELLED);

                sale.getAccountsReceivable().getCollections().forEach(collection -> {
                    if (!"ANULADO".equals(collection.getStatus())) {
                        collection.setStatus("ANULADO");
                    }
                });
            }

            saleRepository.save(sale);
            log.info("Venta ID {} anulada.", id);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}