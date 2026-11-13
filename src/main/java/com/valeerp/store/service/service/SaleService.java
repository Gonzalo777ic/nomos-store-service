package com.valeerp.store.service.service;

import com.valeerp.store.service.model.*;
import com.valeerp.store.service.repository.PromotionRepository;
import com.valeerp.store.service.repository.SaleDetailRepository;
import com.valeerp.store.service.repository.SaleRepository;
import com.valeerp.store.service.repository.TaxRateRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.sales.CatalogClient;
import com.valeerp.sales.Discount;
import com.valeerp.sales.PricedLine;
import com.valeerp.sales.SaleTotals;
import com.valeerp.sales.StockClient;
import com.valeerp.store.service.controller.SaleController.ReferenceDTO;
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
    private final com.valeerp.sales.CommissionService commissionService;

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
        List<PricedLine> drafts = new ArrayList<>();
        for (RegisterSale.Line line : request.details()) {
            CatalogClient.CatalogPrice catalogPrice = catalogClient.get(line.productId());
            TaxRate taxRate = taxRateRepository.findById(line.taxRateId())
                    .orElseThrow(() -> new DomainException("La tasa de impuesto no existe"));
            Promotion promotion = line.promotionId() == null ? null : promotionRepository.findById(line.promotionId()).orElse(null);
            Discount lineDiscount = Discount.optional(line.lineDiscountKind(), line.lineDiscountValue());
            drafts.add(PricedLine.draft(
                    Money.of(catalogPrice.price(), currency),
                    line.quantity(),
                    taxRate.rateAsDecimal(),
                    promotion == null ? null : promotion.getType(),
                    promotion == null ? null : BigDecimal.valueOf(promotion.getDiscountValue()),
                    lineDiscount
            ));
        }
        Discount globalDiscount = Discount.optional(request.globalDiscountKind(), request.globalDiscountValue());
        SaleTotals totals = SaleTotals.compute(drafts, globalDiscount);
        List<PricedLine> priced = totals.lines();

        LocalDate finalDueDate = calculateDueDate(request.saleDate(), request.creditStartDate(), request.numberOfInstallments(), paymentCondition);
        Money totalMoney = totals.net();

        Sale newSale = Sale.builder()
                .clientId(request.clientId())
                .saleDate(request.saleDate())
                .type(saleType)
                .paymentCondition(paymentCondition)
                .sellerId(request.sellerId())
                .totalAmount(totals.net().amount())
                .totalDiscount(totals.totalDiscount().amount())
                .globalDiscount(totals.globalDiscount().amount())
                .taxAmount(totals.tax().amount())
                .baseAmount(totals.base().amount())
                .salesTeamId(request.salesTeamId())
                .territoryId(request.territoryId())
                .commissionRuleId(request.commissionRuleId())
                .status(SaleStatus.EMITIDA)
                .dueDate(LocalDateTime.of(finalDueDate, request.saleDate().toLocalTime()))
                .creditDays(request.creditDays())
                .build();

        Sale savedSale = saleRepository.save(newSale);

        AccountsReceivable ar = AccountsReceivable.open(savedSale, totalMoney);

        LocalDate baseDate = request.creditStartDate() != null
                ? request.creditStartDate()
                : request.saleDate().toLocalDate();

        for (Installment installment : generateInstallments(
                paymentCondition,
                request.numberOfInstallments(),
                baseDate,
                totalMoney
        )) {
            ar.addInstallment(installment);
        }

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
            detail.setLineDiscount(pricedLine.promotionDiscount().plus(pricedLine.lineDiscount()).amount());
            detail.setGlobalDiscountShare(pricedLine.globalDiscountShare().amount());
            detail.setTaxAmount(pricedLine.tax().amount());
            detail.setBaseAmount(pricedLine.base().amount());
            detail.setTaxRateId(line.taxRateId());
            detail.setPromotionId(line.promotionId());
            saleDetailRepository.save(detail);
        }

        try {
            stockClient.issue(savedSale.getId(), "Venta " + savedSale.getId(), request.details().stream()
                    .map(line -> new StockClient.StockLine(line.productId(), line.quantity()))
                    .toList());
            accountingService.recordSale(savedSale, totals.tax(), totals.base());
            if (request.commissionRuleId() != null) {
                commissionService.accrueForSale(
                        savedSale.getId(),
                        savedSale.getSellerId(),
                        request.commissionRuleId(),
                        totals.base()
                );
            }
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
            sale.getAccountsReceivable().cancel();
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

    private List<Installment> generateInstallments(PaymentConditionEnum condition, Integer numberOfInstallments,
                                                   LocalDate baseDate, Money totalAmount) {
        List<Installment> installments = new ArrayList<>();
        if (condition == PaymentConditionEnum.CONTADO) {
            installments.add(Installment.schedule(1, baseDate, totalAmount));
            return installments;
        }
        int n = (numberOfInstallments != null && numberOfInstallments > 0) ? numberOfInstallments : 1;
        BigDecimal share = totalAmount.amount()
                .divide(BigDecimal.valueOf(n), Money.SCALE, Money.ROUNDING);
        Money accumulated = Money.zero(totalAmount.currency());
        for (int i = 1; i <= n; i++) {
            Money current;
            if (i == n) {
                current = totalAmount.minus(accumulated);
            } else {
                current = Money.of(share, totalAmount.currency());
                accumulated = accumulated.plus(current);
            }
            installments.add(Installment.schedule(i, baseDate.plusMonths(i), current));
        }
        return installments;
    }
}