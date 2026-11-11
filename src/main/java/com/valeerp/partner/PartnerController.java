package com.valeerp.partner;

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

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PartnerService partnerService;
    private final PartnerDocumentPolicy documentPolicy;

    public PartnerController(PartnerService partnerService, PartnerDocumentPolicy documentPolicy) {
        this.partnerService = partnerService;
        this.documentPolicy = documentPolicy;
    }

    @GetMapping("/types")
    public List<CatalogItem> types() {
        return Arrays.stream(PartnerType.values())
                .map(type -> new CatalogItem(type.name(), type.label()))
                .toList();
    }

    @GetMapping("/document-types")
    public List<PartnerDocumentPolicy.DocumentTypeView> documentTypes() {
        return documentPolicy.catalog();
    }

    @GetMapping("/address-kinds")
    public List<CatalogItem> addressKinds() {
        return Arrays.stream(AddressKind.values())
                .map(kind -> new CatalogItem(kind.name(), kind.label()))
                .toList();
    }

    @GetMapping("/bank-account-purposes")
    public List<CatalogItem> bankAccountPurposes() {
        return Arrays.stream(BankAccountPurpose.values())
                .map(purpose -> new CatalogItem(purpose.name(), purpose.label()))
                .toList();
    }

    @GetMapping
    public PageResponse<PartnerResponse> list(
            @RequestParam(required = false) String type,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        PartnerType partnerType = type == null || type.isBlank() ? null : Partner.requireType(type);
        return PageResponse.from(partnerService.list(partnerType, pageable).map(PartnerResponse::from));
    }

    @GetMapping("/{id:\\d+}")
    public PartnerResponse get(@PathVariable Long id) {
        return PartnerResponse.from(partnerService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerResponse create(@RequestBody PartnerCreateRequest request) {
        return PartnerResponse.from(partnerService.create(request));
    }

    @PutMapping("/{id:\\d+}")
    public PartnerResponse update(@PathVariable Long id, @RequestBody PartnerUpdateRequest request) {
        return PartnerResponse.from(partnerService.update(id, request));
    }

    @PostMapping("/{id:\\d+}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        partnerService.retire(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerAddressResponse addAddress(@PathVariable Long id, @RequestBody PartnerAddressRequest request) {
        return PartnerAddressResponse.from(partnerService.addAddress(id, request));
    }

    @PostMapping("/{id:\\d+}/addresses/{addressId:\\d+}/retire")
    public ResponseEntity<Void> retireAddress(@PathVariable Long id, @PathVariable Long addressId) {
        partnerService.retireAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/contacts")
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerContactResponse addContact(@PathVariable Long id, @RequestBody PartnerContactRequest request) {
        return PartnerContactResponse.from(partnerService.addContact(id, request));
    }

    @PostMapping("/{id:\\d+}/contacts/{contactId:\\d+}/retire")
    public ResponseEntity<Void> retireContact(@PathVariable Long id, @PathVariable Long contactId) {
        partnerService.retireContact(id, contactId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public TagView addTag(@PathVariable Long id, @RequestBody TagRequest request) {
        PartnerTag tag = partnerService.addTag(id, request.label());
        return new TagView(tag.getId(), tag.getLabel());
    }

    @PostMapping("/{id:\\d+}/tags/{tagId:\\d+}/retire")
    public ResponseEntity<Void> retireTag(@PathVariable Long id, @PathVariable Long tagId) {
        partnerService.retireTag(id, tagId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/commercial")
    public PartnerResponse assignCommercial(@PathVariable Long id, @RequestBody PartnerCommercialRequest request) {
        return PartnerResponse.from(partnerService.assignCommercial(id, request));
    }

    @PostMapping("/{id:\\d+}/bank-accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerBankAccountResponse addBankAccount(
            @PathVariable Long id, @RequestBody PartnerBankAccountRequest request) {
        return PartnerBankAccountResponse.from(partnerService.addBankAccount(id, request));
    }

    @PostMapping("/{id:\\d+}/bank-accounts/{bankAccountId:\\d+}/retire")
    public ResponseEntity<Void> retireBankAccount(@PathVariable Long id, @PathVariable Long bankAccountId) {
        partnerService.retireBankAccount(id, bankAccountId);
        return ResponseEntity.noContent().build();
    }

    public record CatalogItem(String key, String label) {
    }

    public record TagRequest(String label) {
    }

    public record TagView(Long id, String label) {
    }
}
