package com.valeerp.partner;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerCategoryRepository categoryRepository;
    private final LocalPriceListRefRepository priceListRefRepository;
    private final PaymentTermRepository paymentTermRepository;
    private final TimeProvider timeProvider;
    private final PartnerDocumentPolicy documentPolicy;

    public PartnerService(PartnerRepository partnerRepository,
                          PartnerCategoryRepository categoryRepository,
                          LocalPriceListRefRepository priceListRefRepository,
                          PaymentTermRepository paymentTermRepository,
                          TimeProvider timeProvider,
                          PartnerDocumentPolicy documentPolicy) {
        this.partnerRepository = partnerRepository;
        this.categoryRepository = categoryRepository;
        this.priceListRefRepository = priceListRefRepository;
        this.paymentTermRepository = paymentTermRepository;
        this.timeProvider = timeProvider;
        this.documentPolicy = documentPolicy;
    }

    public Page<Partner> list(PartnerType type, Pageable pageable) {
        if (type == null) {
            return partnerRepository.findAll(pageable);
        }
        return partnerRepository.findByPartnerType(type, pageable);
    }

    public Partner require(Long id) {
        return partnerRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Partner no encontrado"));
    }

    @Transactional
    public Partner create(PartnerCreateRequest request) {
        PartnerType type = Partner.requireType(request.partnerType());
        IdentityDocument document = documentPolicy.require(request.documentType(), request.documentNumber());
        ensureDocumentAvailable(document.type(), document.number(), null);

        Partner partner = Partner.register(
                type,
                request.name(),
                request.tradeName(),
                document.type(),
                document.number(),
                request.email(),
                request.phone(),
                timeProvider.now()
        );
        applyAddresses(partner, request.addresses());
        applyContacts(partner, request.contacts());
        applyTags(partner, request.tags());
        return partnerRepository.save(partner);
    }

    @Transactional
    public Partner update(Long id, PartnerUpdateRequest request) {
        Partner partner = require(id);
        PartnerType type = Partner.requireType(request.partnerType());
        IdentityDocument document = documentPolicy.require(request.documentType(), request.documentNumber());
        ensureDocumentAvailable(document.type(), document.number(), id);

        partner.changeType(type);
        partner.rename(request.name(), request.tradeName());
        partner.changeDocument(document.type(), document.number());
        partner.changeContactChannels(request.email(), request.phone());
        return partnerRepository.save(partner);
    }

    @Transactional
    public void retire(Long id) {
        Partner partner = require(id);
        partner.archive(Actors.currentActor(), timeProvider.now());
        partnerRepository.save(partner);
    }

    @Transactional
    public PartnerAddress addAddress(Long partnerId, PartnerAddressRequest request) {
        Partner partner = require(partnerId);
        PartnerAddress address = partner.addAddress(
                AddressKind.require(request.kind()),
                request.line(),
                request.city(),
                request.region(),
                request.postalCode(),
                request.country(),
                Boolean.TRUE.equals(request.primary())
        );
        partnerRepository.save(partner);
        return address;
    }

    @Transactional
    public void retireAddress(Long partnerId, Long addressId) {
        Partner partner = require(partnerId);
        partner.retireAddress(addressId, Actors.currentActor(), timeProvider.now());
        partnerRepository.save(partner);
    }

    @Transactional
    public PartnerContact addContact(Long partnerId, PartnerContactRequest request) {
        Partner partner = require(partnerId);
        PartnerContact contact = partner.addContact(
                request.fullName(),
                request.email(),
                request.phone(),
                request.roleLabel()
        );
        partnerRepository.save(partner);
        return contact;
    }

    @Transactional
    public void retireContact(Long partnerId, Long contactId) {
        Partner partner = require(partnerId);
        partner.retireContact(contactId, Actors.currentActor(), timeProvider.now());
        partnerRepository.save(partner);
    }

    @Transactional
    public PartnerTag addTag(Long partnerId, String label) {
        Partner partner = require(partnerId);
        PartnerTag tag = partner.addTag(label);
        partnerRepository.save(partner);
        return tag;
    }

    @Transactional
    public void retireTag(Long partnerId, Long tagId) {
        Partner partner = require(partnerId);
        partner.retireTag(tagId, Actors.currentActor(), timeProvider.now());
        partnerRepository.save(partner);
    }

    @Transactional
    public Partner assignCommercial(Long partnerId, PartnerCommercialRequest request) {
        Partner partner = require(partnerId);
        if (request.categoryId() != null) {
            PartnerCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new DomainException(
                            DomainExceptionHandler.NOT_FOUND_CODE, "Categoría no encontrada"));
            partner.assignCategory(category);
        }
        if (request.priceListId() != null) {
            LocalPriceListRef priceList = priceListRefRepository.findById(request.priceListId())
                    .orElseThrow(() -> new DomainException(
                            DomainExceptionHandler.NOT_FOUND_CODE, "Lista de precios local no encontrada"));
            partner.assignPriceList(priceList);
        }
        if (request.paymentTermId() != null) {
            PaymentTerm term = paymentTermRepository.findById(request.paymentTermId())
                    .orElseThrow(() -> new DomainException(
                            DomainExceptionHandler.NOT_FOUND_CODE, "Condición de pago no encontrada"));
            partner.assignPaymentTerm(term);
        }
        if (Boolean.TRUE.equals(request.clearCreditLimit())) {
            partner.assignCreditLimit(null);
        } else if (request.creditLimitAmount() != null) {
            String currency = request.creditLimitCurrency() == null || request.creditLimitCurrency().isBlank()
                    ? PartnerCommercePolicy.DEFAULT_CURRENCY
                    : request.creditLimitCurrency().trim().toUpperCase();
            partner.assignCreditLimit(Money.of(request.creditLimitAmount(), currency));
        }
        return partnerRepository.save(partner);
    }

    @Transactional
    public PartnerBankAccount addBankAccount(Long partnerId, PartnerBankAccountRequest request) {
        Partner partner = require(partnerId);
        String currency = request.currencyCode() == null || request.currencyCode().isBlank()
                ? PartnerCommercePolicy.DEFAULT_CURRENCY
                : request.currencyCode();
        PartnerBankAccount account = partner.addBankAccount(
                request.bankCode(),
                request.bankName(),
                request.accountNumber(),
                request.cci(),
                currency,
                request.holderName(),
                BankAccountPurpose.require(request.purpose()),
                Boolean.TRUE.equals(request.primary())
        );
        partnerRepository.save(partner);
        return account;
    }

    @Transactional
    public void retireBankAccount(Long partnerId, Long bankAccountId) {
        Partner partner = require(partnerId);
        partner.retireBankAccount(bankAccountId, Actors.currentActor(), timeProvider.now());
        partnerRepository.save(partner);
    }

    private void applyAddresses(Partner partner, List<PartnerAddressRequest> addresses) {
        if (addresses == null) {
            return;
        }
        for (PartnerAddressRequest request : addresses) {
            partner.addAddress(
                    AddressKind.require(request.kind()),
                    request.line(),
                    request.city(),
                    request.region(),
                    request.postalCode(),
                    request.country(),
                    Boolean.TRUE.equals(request.primary())
            );
        }
    }

    private void applyContacts(Partner partner, List<PartnerContactRequest> contacts) {
        if (contacts == null) {
            return;
        }
        for (PartnerContactRequest request : contacts) {
            partner.addContact(request.fullName(), request.email(), request.phone(), request.roleLabel());
        }
    }

    private void applyTags(Partner partner, List<String> tags) {
        if (tags == null) {
            return;
        }
        for (String label : tags) {
            partner.addTag(label);
        }
    }

    private void ensureDocumentAvailable(DocumentType documentType, String documentNumber, Long excludeId) {
        partnerRepository.findByDocumentTypeAndDocumentNumber(documentType, documentNumber)
                .filter(existing -> excludeId == null || !excludeId.equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe un partner con el documento "
                            + documentType.name() + " " + documentNumber);
                });
    }
}
