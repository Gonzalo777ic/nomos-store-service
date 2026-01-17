package com.nomos.store.service.controller;

import com.nomos.store.service.model.SaleReturn;
import com.nomos.store.service.model.SaleReturnType;
import com.nomos.store.service.service.SaleReturnService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/store/sale-returns")
public class SaleReturnController {

    @Autowired
    private SaleReturnService saleReturnService;





    @Data
    public static class ReturnRequest {
        private Long saleId;
        private String reason;
        private SaleReturnType type;

        private List<ReturnItemRequest> items;
    }

    @Data
    public static class ReturnItemRequest {
        private Long originalDetailId;
        private Integer quantity;
    }


    @PostMapping
    public ResponseEntity<SaleReturn> createDraft(@RequestBody ReturnRequest request) {

        Map<Long, Integer> itemsMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        ReturnItemRequest::getOriginalDetailId,
                        ReturnItemRequest::getQuantity
                ));

        SaleReturn newReturn = saleReturnService.createDraft(
                request.getSaleId(),
                request.getReason(),
                request.getType(),
                itemsMap
        );

        return ResponseEntity.ok(newReturn);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SaleReturn> confirmReturn(@PathVariable Long id) {
        SaleReturn confirmedReturn = saleReturnService.confirmReturn(id);
        return ResponseEntity.ok(confirmedReturn);
    }

    @GetMapping
    public ResponseEntity<List<SaleReturn>> getAllReturns() {
        return ResponseEntity.ok(saleReturnService.findAll());
    }

    @GetMapping("/sale/{saleId}")
    public ResponseEntity<List<SaleReturn>> getReturnsBySale(@PathVariable Long saleId) {
        return ResponseEntity.ok(saleReturnService.findBySaleId(saleId));
    }


}