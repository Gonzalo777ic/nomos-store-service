package com.valeerp.treasury;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/treasury/mass-vendor-payments")
public class MassVendorPaymentController {

    private final MassVendorPaymentService massVendorPaymentService;

    public MassVendorPaymentController(MassVendorPaymentService massVendorPaymentService) {
        this.massVendorPaymentService = massVendorPaymentService;
    }

    @GetMapping
    public PageResponse<MassVendorPaymentResponse> list(
            @RequestParam Long companyId,
            @PageableDefault(size = TreasuryPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(
                massVendorPaymentService.list(companyId, pageable).map(MassVendorPaymentResponse::from));
    }

    @GetMapping("/{id}")
    public MassVendorPaymentResponse get(@PathVariable Long id) {
        return MassVendorPaymentResponse.from(massVendorPaymentService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MassVendorPaymentResponse create(@RequestBody MassVendorPaymentCreateRequest request) {
        return MassVendorPaymentResponse.from(massVendorPaymentService.create(request));
    }

    @PostMapping("/{id}/confirm")
    public MassVendorPaymentResponse confirm(@PathVariable Long id) {
        return MassVendorPaymentResponse.from(massVendorPaymentService.confirm(id));
    }

    @PostMapping("/{id}/post")
    public MassVendorPaymentResponse post(@PathVariable Long id) {
        return MassVendorPaymentResponse.from(massVendorPaymentService.post(id));
    }

    @PostMapping("/{id}/cancel")
    public MassVendorPaymentResponse cancel(@PathVariable Long id) {
        return MassVendorPaymentResponse.from(massVendorPaymentService.cancel(id));
    }
}
