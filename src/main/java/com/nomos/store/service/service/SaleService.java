package com.nomos.store.service.service;

import com.nomos.store.service.controller.SaleController.SaleCreationRequest;
import com.nomos.store.service.controller.SaleController.SaleRequestDetail;
import com.nomos.store.service.controller.SaleController.ReferenceDTO;
import com.nomos.store.service.model.*;
import com.nomos.store.service.repository.SaleDetailRepository;
import com.nomos.store.service.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final AccountingService accountingService; // Inyección del Servicio Contable

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

    public List<ReferenceDTO> getSaleTypes() {
        return Arrays.stream(SaleTypeEnum.values())
                .map(st -> new ReferenceDTO(st.name(), st.getDescription()))
                .collect(Collectors.toList());
    }

    public List<ReferenceDTO> getPaymentConditions() {
        return Arrays.stream(PaymentConditionEnum.values())
                .map(pc -> new ReferenceDTO(pc.name(), pc.getDescription()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Sale createSale(SaleCreationRequest request) {
        log.info("Creando venta. Emisión: {}", request.getSaleDate());

        if (request.getSellerId() == null || request.getSaleDate() == null) {
            throw new IllegalArgumentException("Faltan datos obligatorios: Vendedor o Fecha");
        }
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }

        double totalAmount = request.getDetails().stream()
                .mapToDouble(SaleRequestDetail::getSubtotal)
                .sum();

        SaleTypeEnum saleType = SaleTypeEnum.valueOf(request.getType().toUpperCase());
        PaymentConditionEnum paymentCondition = PaymentConditionEnum.valueOf(request.getPaymentCondition().toUpperCase());

        LocalDate finalDueDate = calculateDueDate(request, paymentCondition);

        Sale newSale = Sale.builder()
                .clientId(request.getClientId())
                .saleDate(request.getSaleDate())
                .type(saleType)
                .paymentCondition(paymentCondition)
                .sellerId(request.getSellerId())
                .totalAmount(totalAmount)
                .totalDiscount(0.0)
                .status("EMITIDA")
                .dueDate(LocalDateTime.of(finalDueDate, request.getSaleDate().toLocalTime()))
                .creditDays(request.getCreditDays())
                .build();

        Sale savedSale = saleRepository.save(newSale);

        AccountsReceivable ar = AccountsReceivable.builder()
                .sale(savedSale)
                .totalAmount(totalAmount)
                .status(AccountsReceivableStatus.ACTIVE)
                .build();

        LocalDate baseDate = (request.getCreditStartDate() != null)
                ? request.getCreditStartDate()
                : request.getSaleDate().toLocalDate();

        List<Installment> installments = generateInstallments(
                ar,
                paymentCondition,
                request.getNumberOfInstallments(),
                baseDate,
                totalAmount
        );
        ar.setInstallments(installments);

        savedSale.setAccountsReceivable(ar);
        savedSale = saleRepository.save(savedSale);

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

        generateAccountingEntry(savedSale);

        log.info("Venta #{} registrada exitosamente.", savedSale.getId());
        return savedSale;
    }

    @Transactional
    public void cancelSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        if ("CANCELADA".equals(sale.getStatus())) {
            throw new IllegalArgumentException("La venta ya está cancelada.");
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
    }




    /**
     * Calcula la fecha de vencimiento final basada en la condición de pago.
     */
    private LocalDate calculateDueDate(SaleCreationRequest request, PaymentConditionEnum condition) {
        if (condition == PaymentConditionEnum.CONTADO) {
            return request.getSaleDate().toLocalDate();
        } else {

            LocalDate baseDate = (request.getCreditStartDate() != null)
                    ? request.getCreditStartDate()
                    : request.getSaleDate().toLocalDate();

            int months = (request.getNumberOfInstallments() != null && request.getNumberOfInstallments() > 0)
                    ? request.getNumberOfInstallments()
                    : 1;

            return baseDate.plusMonths(months);
        }
    }

    /**
     * Genera la lista de cuotas (Installments).
     */
    private List<Installment> generateInstallments(AccountsReceivable ar, PaymentConditionEnum condition, Integer numberOfInstallments, LocalDate baseDate, Double totalAmount) {
        List<Installment> installments = new ArrayList<>();

        if (condition == PaymentConditionEnum.CONTADO) {
            installments.add(Installment.builder()
                    .accountsReceivable(ar)
                    .number(1)
                    .expectedAmount(totalAmount)
                    .dueDate(baseDate)
                    .status(InstallmentStatus.PENDING)
                    .build());
        } else {
            int n = (numberOfInstallments != null && numberOfInstallments > 0) ? numberOfInstallments : 1;
            double rawAmount = totalAmount / n;
            double roundedAmount = Math.round(rawAmount * 100.0) / 100.0;
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
                        .dueDate(baseDate.plusMonths(i))
                        .status(InstallmentStatus.PENDING)
                        .build());
            }
        }
        return installments;
    }

    /**
     * Genera el asiento contable de venta.
     */
    private void generateAccountingEntry(Sale sale) {
            AccountingJournalEntry entry = new AccountingJournalEntry();
            entry.setEntryDate(LocalDateTime.now());
            entry.setConcept("Venta " + sale.getType() + " #" + sale.getId());
            entry.setReferenceDocument("SALE-" + sale.getId());
            entry.setStatus("POSTED");

            List<AccountingJournalLine> lines = new ArrayList<>();

            lines.add(AccountingJournalLine.builder()
                    .accountCode("12.1")
                    .accountName("Facturas por Cobrar")
                    .debit(sale.getTotalAmount())
                    .credit(0.0)
                    .build());

            double baseImponible = sale.getTotalAmount() / 1.18; // Asumiendo IGV 18%
            double igv = sale.getTotalAmount() - baseImponible;

            lines.add(AccountingJournalLine.builder()
                    .accountCode("40.1")
                    .accountName("Tributos por Pagar (IGV)")
                    .debit(0.0)
                    .credit(Math.round(igv * 100.0) / 100.0)
                    .build());

            lines.add(AccountingJournalLine.builder()
                    .accountCode("70.1")
                    .accountName("Venta de Mercaderías")
                    .debit(0.0)
                    .credit(Math.round(baseImponible * 100.0) / 100.0)
                    .build());

            entry.setLines(lines);

            accountingService.createEntry(entry);
    }
}