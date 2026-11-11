package com.valeerp.sales;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/sales/orders")
public class SaleOrderController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final SaleOrderService saleOrderService;

    public SaleOrderController(SaleOrderService saleOrderService) {
        this.saleOrderService = saleOrderService;
    }

    @GetMapping("/invoicing-policies")
    public List<CatalogItem> invoicingPolicies() {
        return Arrays.stream(InvoicingPolicy.values())
                .map(policy -> new CatalogItem(policy.name(), policy.name()))
                .toList();
    }

    @GetMapping
    public PageResponse<SaleOrderResponse> list(
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long opportunityId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        SaleOrderStatus parsed = SaleOrderService.parseStatus(status);
        return PageResponse.from(
                saleOrderService.list(partnerId, opportunityId, parsed, pageable).map(SaleOrderResponse::from)
        );
    }

    @GetMapping("/{id}")
    public SaleOrderResponse get(@PathVariable Long id) {
        return SaleOrderResponse.from(saleOrderService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleOrderResponse create(@RequestBody SaleOrderCreateRequest request) {
        return SaleOrderResponse.from(saleOrderService.createQuotation(request));
    }

    @PutMapping("/{id}")
    public SaleOrderResponse update(@PathVariable Long id, @RequestBody SaleOrderUpdateRequest request) {
        return SaleOrderResponse.from(saleOrderService.updateDraft(id, request));
    }

    @PostMapping("/{id}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleOrderResponse addLine(@PathVariable Long id, @RequestBody SaleOrderLineRequest request) {
        return SaleOrderResponse.from(saleOrderService.addLine(id, request));
    }

    @PostMapping("/{id}/lines/{lineId}/promotions")
    public SaleOrderResponse applyPromotion(@PathVariable Long id,
                                            @PathVariable Long lineId,
                                            @RequestBody ApplyPromotionRequest request) {
        return SaleOrderResponse.from(saleOrderService.applyPromotion(id, lineId, request));
    }

    @PostMapping("/{id}/lines/{lineId}/promotions/clear")
    public SaleOrderResponse clearPromotion(@PathVariable Long id, @PathVariable Long lineId) {
        return SaleOrderResponse.from(saleOrderService.clearPromotion(id, lineId));
    }

    @PostMapping("/{id}/lines/{lineId}/discount")
    public SaleOrderResponse applyLineDiscount(@PathVariable Long id,
                                               @PathVariable Long lineId,
                                               @RequestBody DiscountRequest request) {
        return SaleOrderResponse.from(saleOrderService.applyLineDiscount(
                id, lineId, request.kind(), request.value()));
    }

    @PostMapping("/{id}/global-discount")
    public SaleOrderResponse applyGlobalDiscount(@PathVariable Long id, @RequestBody DiscountRequest request) {
        return SaleOrderResponse.from(saleOrderService.applyGlobalDiscount(id, request.kind(), request.value()));
    }

    @PostMapping("/{id}/lines/{lineId}/remove")
    public SaleOrderResponse removeLine(@PathVariable Long id, @PathVariable Long lineId) {
        return SaleOrderResponse.from(saleOrderService.removeLine(id, lineId));
    }

    @PostMapping("/{id}/confirm")
    public SaleOrderResponse confirm(@PathVariable Long id) {
        return SaleOrderResponse.from(saleOrderService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    public SaleOrderResponse cancel(@PathVariable Long id) {
        return SaleOrderResponse.from(saleOrderService.cancel(id));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> rejectStatusMutation() {
        throw new com.valeerp.kernel.DomainException(
                "Use /confirm o /cancel; no se admite setStatus desde el controlador"
        );
    }

    public record CatalogItem(String key, String label) {
    }

    public record DiscountRequest(String kind, java.math.BigDecimal value) {
    }
}
