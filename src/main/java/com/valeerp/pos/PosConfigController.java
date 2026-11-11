package com.valeerp.pos;

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

@RestController
@RequestMapping("/api/pos/configs")
public class PosConfigController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PosConfigService configService;

    public PosConfigController(PosConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public PageResponse<PosConfigResponse> list(
            @RequestParam(required = false) Long companyId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(configService.list(companyId, pageable).map(PosConfigResponse::from));
    }

    @GetMapping("/{id}")
    public PosConfigResponse get(@PathVariable Long id) {
        return PosConfigResponse.from(configService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PosConfigResponse create(@RequestBody PosConfigCreateRequest request) {
        return PosConfigResponse.from(configService.create(request));
    }

    @PutMapping("/{id}")
    public PosConfigResponse update(@PathVariable Long id, @RequestBody PosConfigUpdateRequest request) {
        return PosConfigResponse.from(configService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        configService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
