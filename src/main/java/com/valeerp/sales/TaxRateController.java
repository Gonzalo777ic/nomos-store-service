package com.valeerp.sales;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo fiscal local. Baja lógica vía retire; sin delete físico.
 */
@RestController
@RequestMapping("/api/store/tax-rates")
public class TaxRateController {

    private final TaxRateService taxRateService;

    public TaxRateController(TaxRateService taxRateService) {
        this.taxRateService = taxRateService;
    }

    @GetMapping("/kinds")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public List<CatalogItem> kinds() {
        return Arrays.stream(TaxKind.values())
                .map(kind -> new CatalogItem(kind.name(), kind.label()))
                .toList();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public List<TaxRateResponse> list() {
        return taxRateService.listActive().stream().map(TaxRateResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public TaxRateResponse get(@PathVariable Long id) {
        return TaxRateResponse.from(taxRateService.require(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public TaxRateResponse create(@RequestBody TaxRateRequest request) {
        return TaxRateResponse.from(taxRateService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public TaxRateResponse update(@PathVariable Long id, @RequestBody TaxRateRequest request) {
        return TaxRateResponse.from(taxRateService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        taxRateService.retire(id);
        return ResponseEntity.noContent().build();
    }

    public record CatalogItem(String key, String label) {
    }
}
