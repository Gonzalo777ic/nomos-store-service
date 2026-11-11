package com.nomos.store.service.controller;

import com.nomos.store.service.model.SaleReturnType;
import com.nomos.store.service.service.SaleReturnService;
import com.valeerp.sales.SaleReturnResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/store/sale-returns")
public class SaleReturnController {

    private final SaleReturnService saleReturnService;

    public SaleReturnController(SaleReturnService saleReturnService) {
        this.saleReturnService = saleReturnService;
    }

    public record ReturnItemRequest(Long originalDetailId, Integer quantity) {
    }

    public record ReturnRequest(Long saleId, String reason, SaleReturnType type, List<ReturnItemRequest> items) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleReturnResponse createDraft(@RequestBody ReturnRequest request) {
        Map<Long, Integer> itemsMap = request.items().stream()
                .collect(Collectors.toMap(ReturnItemRequest::originalDetailId, ReturnItemRequest::quantity));
        return SaleReturnResponse.from(saleReturnService.createDraft(
                request.saleId(), request.reason(), request.type(), itemsMap));
    }

    @PostMapping("/{id}/confirm")
    public SaleReturnResponse confirmReturn(@PathVariable Long id) {
        return SaleReturnResponse.from(saleReturnService.confirmReturn(id));
    }

    @PostMapping("/{id}/reverse")
    public SaleReturnResponse reverse(@PathVariable Long id) {
        return SaleReturnResponse.from(saleReturnService.reverseConfirmed(id));
    }

    @PostMapping("/{id}/cancel")
    public SaleReturnResponse cancelDraft(@PathVariable Long id) {
        return SaleReturnResponse.from(saleReturnService.cancelDraft(id));
    }

    @GetMapping
    public List<SaleReturnResponse> getAllReturns() {
        return saleReturnService.findAll().stream().map(SaleReturnResponse::from).toList();
    }

    @GetMapping("/sale/{saleId}")
    public List<SaleReturnResponse> getReturnsBySale(@PathVariable Long saleId) {
        return saleReturnService.findBySaleId(saleId).stream().map(SaleReturnResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleReturnResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(SaleReturnResponse.from(saleReturnService.require(id)));
    }
}
