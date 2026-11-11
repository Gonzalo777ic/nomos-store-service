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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/sales/promotions")
public class CatalogPromotionController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CatalogPromotionService promotionService;

    public CatalogPromotionController(CatalogPromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/kinds")
    public List<CatalogItem> kinds() {
        return Arrays.stream(PromotionKind.values())
                .map(kind -> new CatalogItem(kind.name(), kind.name()))
                .toList();
    }

    @GetMapping
    public PageResponse<CatalogPromotionResponse> list(
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(promotionService.list(pageable).map(CatalogPromotionResponse::from));
    }

    @GetMapping("/{id}")
    public CatalogPromotionResponse get(@PathVariable Long id) {
        return CatalogPromotionResponse.from(promotionService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogPromotionResponse create(@RequestBody CatalogPromotionCreateRequest request) {
        return CatalogPromotionResponse.from(promotionService.create(request));
    }

    @PutMapping("/{id}")
    public CatalogPromotionResponse update(@PathVariable Long id, @RequestBody CatalogPromotionUpdateRequest request) {
        return CatalogPromotionResponse.from(promotionService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        promotionService.retire(id);
        return ResponseEntity.noContent().build();
    }

    public record CatalogItem(String key, String label) {
    }
}
