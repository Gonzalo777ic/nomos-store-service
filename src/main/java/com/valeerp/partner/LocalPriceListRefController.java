package com.valeerp.partner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/partners/price-lists")
public class LocalPriceListRefController {

    private final LocalPriceListRefService priceListRefService;

    public LocalPriceListRefController(LocalPriceListRefService priceListRefService) {
        this.priceListRefService = priceListRefService;
    }

    @GetMapping
    public List<LocalPriceListRefResponse> list() {
        return priceListRefService.list().stream().map(LocalPriceListRefResponse::from).toList();
    }

    @GetMapping("/{id}")
    public LocalPriceListRefResponse get(@PathVariable Long id) {
        return LocalPriceListRefResponse.from(priceListRefService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalPriceListRefResponse create(@RequestBody LocalPriceListRefRequest request) {
        return LocalPriceListRefResponse.from(priceListRefService.create(request));
    }

    @PutMapping("/{id}")
    public LocalPriceListRefResponse update(@PathVariable Long id, @RequestBody LocalPriceListRefRequest request) {
        return LocalPriceListRefResponse.from(priceListRefService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        priceListRefService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
