package com.valeerp.customer;

import com.valeerp.kernel.api.PageResponse;
import com.valeerp.partner.PartnerDocumentPolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

/**
 * Convención de API piloto: solo DTOs in/out, paginación y errores de dominio homogéneos.
 * Catálogo documental delegado a la política canónica de Contactos / Partner.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CustomerService customerService;
    private final PartnerDocumentPolicy documentPolicy;

    public CustomerController(CustomerService customerService, PartnerDocumentPolicy documentPolicy) {
        this.customerService = customerService;
        this.documentPolicy = documentPolicy;
    }

    @GetMapping
    public PageResponse<CustomerResponse> list(
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(customerService.findAll(pageable).map(CustomerResponse::from));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return CustomerResponse.from(customerService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@RequestBody CustomerProfileRequest profile) {
        return CustomerResponse.from(customerService.register(profile));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        customerService.retire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/document-types")
    public List<PartnerDocumentPolicy.DocumentTypeView> documentTypes() {
        return documentPolicy.catalog();
    }
}
