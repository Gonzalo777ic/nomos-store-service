package com.valeerp.sales;

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

import java.util.List;

@RestController
@RequestMapping("/api/sales/commissions")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @GetMapping("/rules")
    public List<CommissionRuleResponse> listRules() {
        return commissionService.listRules().stream().map(CommissionRuleResponse::from).toList();
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public CommissionRuleResponse createRule(@RequestBody CommissionRuleRequest request) {
        return CommissionRuleResponse.from(commissionService.createRule(request));
    }

    @PutMapping("/rules/{id}")
    public CommissionRuleResponse updateRule(@PathVariable Long id, @RequestBody CommissionRuleRequest request) {
        return CommissionRuleResponse.from(commissionService.updateRule(id, request));
    }

    @PostMapping("/rules/{id}/retire")
    public ResponseEntity<Void> retireRule(@PathVariable Long id) {
        commissionService.retireRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<SalesCommissionResponse> list(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Long saleId,
            @RequestParam(required = false) Long saleOrderId) {
        if (sellerId != null) {
            return commissionService.listBySeller(sellerId).stream().map(SalesCommissionResponse::from).toList();
        }
        if (saleId != null) {
            return commissionService.listBySale(saleId).stream().map(SalesCommissionResponse::from).toList();
        }
        if (saleOrderId != null) {
            return commissionService.listBySaleOrder(saleOrderId).stream().map(SalesCommissionResponse::from).toList();
        }
        return List.of();
    }

    @PostMapping("/{id}/cancel")
    public SalesCommissionResponse cancel(@PathVariable Long id) {
        return SalesCommissionResponse.from(commissionService.cancel(id));
    }
}
