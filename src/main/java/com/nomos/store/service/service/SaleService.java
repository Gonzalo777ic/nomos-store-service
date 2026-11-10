package com.nomos.store.service.service;

import com.nomos.store.service.model.*;
import com.nomos.store.service.repository.PromotionRepository;
import com.nomos.store.service.repository.SaleDetailRepository;
import com.nomos.store.service.repository.SaleRepository;
import com.nomos.store.service.repository.TaxRateRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.sales.CatalogClient;
import com.valeerp.sales.PricedLine;
import com.valeerp.sales.StockClient;
import com.nomos.store.service.controller.SaleController.ReferenceDTO;
import com.valeerp.sales.RegisterSale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final AccountingService accountingService;
    private final CatalogClient catalogClient;
    private final StockClient stockClient;
    private final TaxRateRepository taxRateRepository;
    private final PromotionRepository promotionRepository;

    @Value("${valeerp.currency}")
    private String currency;

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
    public Sale createSale(RegisterSale request) {
        log.info("Creando venta. Emisión: {}", request.saleDate());

        if (request.sellerId() == null || request.saleDate() == null) {
            throw new DomainException("Faltan datos obligatorios: vendedor o fecha");
        }
        if (request.details() == null || request.details().isEmpty()) {
            throw new DomainException("La venta debe tener al menos un detalle");
        }

        SaleTypeEnum saleType = SaleTypeEnum.valueOf(request.type().toUpperCase());
        PaymentConditionEnum paymentCondition = PaymentConditionEnum.valueOf(request.paymentCondition().toUpperCase());
        List<PricedLine> priced = new ArrayList<>();
        Money total = Money.zero(currency);
        Money discount = Money.zero(currency);
        Money tax = Money.zero(currency);
        Money base = Money.zero(currency);
        for (RegisterSale.Line line : request.details()) {
            CatalogClient.CatalogPrice catalogPrice = catalogClient.get(line.productId());
            TaxRate taxRate = taxRateRepository.findById(line.taxRateId())
                    .orElseThrow(() -> new DomainException("La tasa de impuesto no existe"));
            Promotion promotion = line.promotionId() == null ? null : promotionRepository.findById(line.promotionId()).orElse(null);
            PricedLine pricedLine = PricedLine.of(
                    Money.of(catalogPrice.price(), currency),
                    line.quantity(),
                    BigDecimal.valueOf(taxRate.getRate()),
                    promotion == null ? null : promotion.getType(),
                    promotion == null ? null : BigDecimal.valueOf(promotion.getDiscountValue())
            );
            priced.add(pricedLine);
            total = total.plus(pricedLine.net());
            discount = discount.plus(pricedLine.discount());
            tax = tax.plus(pricedLine.tax());
            base = base.plus(pricedLine.base());
        }

        LocalDate finalDueDate = calculateDueDate(request.saleDate(), request.creditStartDate(), request.numberOfInstallments(), paymentCondition);
        double totalAmount = total.amount().doubleValue();

        Sale newSale = Sale.builder()
                .clientId(request.clientId())
                .saleDate(request.saleDate())
                .type(saleType)
                .paymentCondition(paymentCondition)
                .sellerId(request.sellerId())
                .totalAmount(total.amount())
                .totalDiscount(discount.amount())
                .status(SaleStatus.EMITIDA)
                .dueDate(LocalDateTime.of(finalDueDate, request.saleDate().toLocalTime()))
                .creditDays(request.creditDays())
                .build();

        Sale savedSale = saleRepository.save(newSale);

        AccountsReceivable ar = AccountsReceivable.builder()
                .sale(savedSale)
                .totalAmount(totalAmount)
                .status(AccountsReceivableStatus.ACTIVE)
                .build();

        LocalDate baseDate = request.creditStartDate() != null
                ? request.creditStartDate()
                : request.saleDate().toLocalDate();

        List<Installment> installments = generateInstallments(
                ar,
                paymentCondition,
                request.numberOfInstallments(),
                baseDate,
                totalAmount
        );
        ar.setInstallments(installments);

        savedSale.setAccountsReceivable(ar);
        savedSale = saleRepository.save(savedSale);

        int index = 0;
        for (RegisterSale.Line line : request.details()) {
            PricedLine pricedLine = priced.get(index++);
            SaleDetail detail = new SaleDetail();
            detail.setSale(savedSale);
            detail.setProductId(line.productId());
            detail.setUnitPrice(pricedLine.gross().amount().divide(BigDecimal.valueOf(line.quantity()), Money.SCALE, Money.ROUNDING));
            detail.setQuantity(line.quantity());
            detail.setSubtotal(pricedLine.net().amount());
            detail.setTaxRateId(line.taxRateId());
            detail.setPromotionId(line.promotionId());
            saleDetailRepository.save(detail);
        }

        try {
            stockClient.issue(savedSale.getId(), "Venta " + savedSale.getId(), request.details().stream()
                    .map(line -> new StockClient.StockLine(line.productId(), line.quantity()))
                    .toList());
            accountingService.recordSale(savedSale, tax, base);
        } catch (RuntimeException exception) {
            stockClient.reverse(savedSale.getId(), "SALES", "Fallo al registrar la venta");
            savedSale.setStatus(SaleStatus.CANCELADA);
            saleRepository.save(savedSale);
            throw exception;
        }

        log.info("Venta #{} registrada.", savedSale.getId());
        return savedSale;
    }

    @Transactional
    public void cancelSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new DomainException("Venta no encontrada"));
        if (sale.getStatus() == SaleStatus.CANCELADA) {
            throw new DomainException("La venta ya está cancelada");
        }
        stockClient.reverse(sale.getId(), "SALES", "Anulación de venta " + sale.getId());
        accountingService.reverseSale(sale);
        sale.setStatus(SaleStatus.CANCELADA);
        if (sale.getAccountsReceivable() != null) {
            sale.getAccountsReceivable().setStatus(AccountsReceivableStatus.CANCELLED);
            if (sale.getAccountsReceivable().getCollections() != null) {
                sale.getAccountsReceivable().getCollections().forEach(collection -> {
                    if (collection.getStatus() != CollectionStatus.CANCELLED) {
                        collection.setStatus(CollectionStatus.CANCELLED);
                    }
                });
            }
        }
        saleRepository.save(sale);
    }

    private LocalDate calculateDueDate(LocalDateTime saleDate, LocalDate creditStartDate, Integer numberOfInstallments, PaymentConditionEnum condition) {
        if (condition == PaymentConditionEnum.CONTADO) {
            return saleDate.toLocalDate();
        }
        LocalDate baseDate = creditStartDate != null ? creditStartDate : saleDate.toLocalDate();
        int months = numberOfInstallments != null && numberOfInstallments > 0 ? numberOfInstallments : 1;
        return baseDate.plusMonths(months);
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
}