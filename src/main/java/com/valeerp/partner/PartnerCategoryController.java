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
@RequestMapping("/api/partners/categories")
public class PartnerCategoryController {

    private final PartnerCategoryService categoryService;

    public PartnerCategoryController(PartnerCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<PartnerCategoryResponse> list() {
        return categoryService.list().stream().map(PartnerCategoryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PartnerCategoryResponse get(@PathVariable Long id) {
        return PartnerCategoryResponse.from(categoryService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerCategoryResponse create(@RequestBody PartnerCategoryRequest request) {
        return PartnerCategoryResponse.from(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public PartnerCategoryResponse update(@PathVariable Long id, @RequestBody PartnerCategoryRequest request) {
        return PartnerCategoryResponse.from(categoryService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        categoryService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
