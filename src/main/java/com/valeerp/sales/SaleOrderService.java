package com.valeerp.sales;

import com.valeerp.crm.CrmOpportunityService;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.partner.PartnerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
public class SaleOrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final CatalogPromotionService promotionService;
    private final TaxRateService taxRateService;
    private final CommissionService commissionService;
    private final PartnerService partnerService;
    private final CrmOpportunityService opportunityService;
    private final TimeProvider timeProvider;

    public SaleOrderService(SaleOrderRepository saleOrderRepository,
                            CatalogPromotionService promotionService,
                            TaxRateService taxRateService,
                            CommissionService commissionService,
                            PartnerService partnerService,
                            CrmOpportunityService opportunityService,
                            TimeProvider timeProvider) {
        this.saleOrderRepository = saleOrderRepository;
        this.promotionService = promotionService;
        this.taxRateService = taxRateService;
        this.commissionService = commissionService;
        this.partnerService = partnerService;
        this.opportunityService = opportunityService;
        this.timeProvider = timeProvider;
    }

    public Page<SaleOrder> list(Long partnerId, Long opportunityId, SaleOrderStatus status, Pageable pageable) {
        if (opportunityId != null) {
            return saleOrderRepository.findByOpportunityId(opportunityId, pageable);
        }
        if (partnerId != null && status != null) {
            return saleOrderRepository.findByPartnerIdAndStatus(partnerId, status, pageable);
        }
        if (partnerId != null) {
            return saleOrderRepository.findByPartnerId(partnerId, pageable);
        }
        if (status != null) {
            return saleOrderRepository.findByStatus(status, pageable);
        }
        return saleOrderRepository.findAll(pageable);
    }

    public SaleOrder require(Long id) {
        return saleOrderRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Pedido de venta no encontrado"));
    }

    @Transactional
    public SaleOrder createQuotation(SaleOrderCreateRequest request) {
        partnerService.require(request.partnerId());
        if (request.opportunityId() != null) {
            opportunityService.require(request.opportunityId());
        }
        Instant now = timeProvider.now();
        Instant validUntil = request.validUntil() == null
                ? now.plus(SalesPolicy.DEFAULT_QUOTATION_VALIDITY_DAYS, ChronoUnit.DAYS)
                : request.validUntil();
        String currency = resolveCurrency(request.currency());
        SaleOrder order = SaleOrder.draft(
                request.partnerId(),
                request.opportunityId(),
                currency,
                validUntil,
                InvoicingPolicy.orDefault(request.invoicingPolicy()),
                request.notes(),
                now
        );
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder updateDraft(Long id, SaleOrderUpdateRequest request) {
        SaleOrder order = require(id);
        if (request.validUntil() != null) {
            order.changeValidity(request.validUntil());
        }
        if (request.invoicingPolicy() != null && !request.invoicingPolicy().isBlank()) {
            order.changeInvoicingPolicy(InvoicingPolicy.require(request.invoicingPolicy()));
        }
        if (request.notes() != null) {
            order.changeNotes(request.notes());
        }
        if (request.globalDiscountKind() != null || request.globalDiscountValue() != null) {
            order.applyGlobalDiscount(Discount.optional(request.globalDiscountKind(), request.globalDiscountValue()));
        }
        if (request.sellerId() != null || request.salesTeamId() != null
                || request.territoryId() != null || request.commissionRuleId() != null) {
            order.assignCommercialContext(
                    request.sellerId() != null ? request.sellerId() : order.getSellerId(),
                    request.salesTeamId() != null ? request.salesTeamId() : order.getSalesTeamId(),
                    request.territoryId() != null ? request.territoryId() : order.getTerritoryId(),
                    request.commissionRuleId() != null ? request.commissionRuleId() : order.getCommissionRuleId()
            );
        }
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder addLine(Long orderId, SaleOrderLineRequest request) {
        SaleOrder order = require(orderId);
        // clientSubtotal se ignora deliberadamente
        Money unitPrice = Money.of(requireUnitPrice(request.unitPrice()), order.getCurrency());
        PromotionSpec promotion = null;
        if (request.promotionId() != null) {
            promotion = promotionService.requireActive(request.promotionId()).toSpec();
        }
        Discount lineDiscount = Discount.optional(request.lineDiscountKind(), request.lineDiscountValue());
        var taxRate = resolveTaxRate(request.taxRateId());
        order.addLine(
                request.productId(),
                request.productName(),
                requireQuantity(request.quantity()),
                unitPrice,
                promotion,
                lineDiscount,
                taxRate.getId(),
                taxRate.rateAsDecimal()
        );
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder applyLineDiscount(Long orderId, Long lineId, String kind, BigDecimal value) {
        SaleOrder order = require(orderId);
        order.applyLineDiscount(lineId, Discount.optional(kind, value));
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder applyGlobalDiscount(Long orderId, String kind, BigDecimal value) {
        SaleOrder order = require(orderId);
        order.applyGlobalDiscount(Discount.optional(kind, value));
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder applyPromotion(Long orderId, Long lineId, ApplyPromotionRequest request) {
        SaleOrder order = require(orderId);
        if (request == null || request.promotionId() == null) {
            throw new DomainException("La promoción es obligatoria");
        }
        CatalogPromotion promotion = promotionService.requireActive(request.promotionId());
        order.applyPromotionToLine(lineId, promotion.toSpec());
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder clearPromotion(Long orderId, Long lineId) {
        SaleOrder order = require(orderId);
        order.clearLinePromotion(lineId);
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder removeLine(Long orderId, Long lineId) {
        SaleOrder order = require(orderId);
        order.removeLine(lineId);
        return saleOrderRepository.save(order);
    }

    @Transactional
    public SaleOrder confirm(Long orderId) {
        SaleOrder order = require(orderId);
        String nextNumber = nextCommercialDocumentNumber();
        order.confirm(nextNumber, timeProvider.now());
        SaleOrder saved = saleOrderRepository.save(order);
        if (saved.getCommissionRuleId() != null && saved.getSellerId() != null) {
            commissionService.accrueForSaleOrder(
                    saved.getId(),
                    saved.getSellerId(),
                    saved.getCommissionRuleId(),
                    saved.base()
            );
        }
        return saved;
    }

    @Transactional
    public SaleOrder cancel(Long orderId) {
        SaleOrder order = require(orderId);
        order.cancel(timeProvider.now());
        return saleOrderRepository.save(order);
    }

    public static SaleOrderStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return SaleOrderStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de pedido no soportado: " + raw);
        }
    }

    private String nextCommercialDocumentNumber() {
        String series = SalesPolicy.SALE_ORDER_DOCUMENT_SERIES;
        long next = saleOrderRepository.findMaxCommercialDocumentNumber(series)
                .map(SaleOrderService::parseDocumentNumber)
                .orElse(0L) + 1L;
        return String.format("%08d", next);
    }

    private static long parseDocumentNumber(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private static String resolveCurrency(String raw) {
        if (raw == null || raw.isBlank()) {
            return SalesPolicy.DEFAULT_CURRENCY;
        }
        return Currency.of(raw).code();
    }

    private static int requireQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("La cantidad debe ser mayor que cero");
        }
        return quantity;
    }

    private static BigDecimal requireUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new DomainException("El precio unitario es obligatorio");
        }
        if (unitPrice.signum() < 0) {
            throw new DomainException("El precio unitario no puede ser negativo");
        }
        return unitPrice;
    }

    private com.valeerp.store.service.model.TaxRate resolveTaxRate(Long taxRateId) {
        if (taxRateId != null) {
            return taxRateService.require(taxRateId);
        }
        return taxRateService.requireByCatalogCode(SalesPolicy.DEFAULT_TAX_CATALOG_CODE);
    }
}
