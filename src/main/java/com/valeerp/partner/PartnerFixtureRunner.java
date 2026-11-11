package com.valeerp.partner;

import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Seed local de partners (persona, empresa, mixtos) sin otros servicios.
 * Activar: {@code valeerp.partner.seed-enabled=true} o perfil {@code partner-demo}.
 */
@Component
@ConditionalOnProperty(prefix = "valeerp.partner", name = "seed-enabled", havingValue = "true")
public class PartnerFixtureRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PartnerFixtureRunner.class);

    private final PartnerRepository partnerRepository;
    private final PartnerCategoryRepository categoryRepository;
    private final PaymentTermRepository paymentTermRepository;
    private final LocalPriceListRefRepository priceListRefRepository;
    private final PartnerDocumentPolicy documentPolicy;
    private final PartnerProperties properties;
    private final TimeProvider timeProvider;

    public PartnerFixtureRunner(PartnerRepository partnerRepository,
                                PartnerCategoryRepository categoryRepository,
                                PaymentTermRepository paymentTermRepository,
                                LocalPriceListRefRepository priceListRefRepository,
                                PartnerDocumentPolicy documentPolicy,
                                PartnerProperties properties,
                                TimeProvider timeProvider) {
        this.partnerRepository = partnerRepository;
        this.categoryRepository = categoryRepository;
        this.paymentTermRepository = paymentTermRepository;
        this.priceListRefRepository = priceListRefRepository;
        this.documentPolicy = documentPolicy;
        this.properties = properties;
        this.timeProvider = timeProvider;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (partnerRepository.count() > 0) {
            log.info("Partner fixtures omitidos: ya hay {} partners", partnerRepository.count());
            return;
        }
        Map<String, PartnerCategory> categories = seedCategories();
        Map<String, PaymentTerm> terms = seedTerms();
        Map<String, LocalPriceListRef> priceLists = seedPriceLists();
        int created = 0;
        for (PartnerFixtureData.PartnerSeed seed : PartnerFixtureData.partners()) {
            seedPartner(seed, categories, terms, priceLists);
            created++;
        }
        log.info("Partner fixtures cargados: {} partners, moneda default {}",
                created, properties.getDefaultCurrency());
    }

    private Map<String, PartnerCategory> seedCategories() {
        Map<String, PartnerCategory> byCode = new HashMap<>();
        for (PartnerFixtureData.CategorySeed seed : PartnerFixtureData.CATEGORIES) {
            PartnerCategory category = categoryRepository.findByCode(seed.code())
                    .orElseGet(() -> categoryRepository.save(
                            PartnerCategory.create(seed.code(), seed.name(), seed.description())));
            byCode.put(category.getCode(), category);
        }
        return byCode;
    }

    private Map<String, PaymentTerm> seedTerms() {
        Map<String, PaymentTerm> byCode = new HashMap<>();
        for (PartnerFixtureData.TermSeed seed : PartnerFixtureData.TERMS) {
            PaymentTerm term = paymentTermRepository.findByCode(seed.code())
                    .orElseGet(() -> paymentTermRepository.save(
                            PaymentTerm.create(seed.code(), seed.name(), seed.daysDue(), seed.description())));
            byCode.put(term.getCode(), term);
        }
        return byCode;
    }

    private Map<String, LocalPriceListRef> seedPriceLists() {
        Map<String, LocalPriceListRef> byCode = new HashMap<>();
        for (PartnerFixtureData.PriceListSeed seed : PartnerFixtureData.PRICE_LISTS) {
            LocalPriceListRef ref = priceListRefRepository.findByCode(seed.code())
                    .orElseGet(() -> priceListRefRepository.save(
                            LocalPriceListRef.create(seed.catalogId(), seed.code(), seed.name())));
            byCode.put(ref.getCode(), ref);
        }
        return byCode;
    }

    private void seedPartner(PartnerFixtureData.PartnerSeed seed,
                             Map<String, PartnerCategory> categories,
                             Map<String, PaymentTerm> terms,
                             Map<String, LocalPriceListRef> priceLists) {
        String documentNumber = seed.documentType() == DocumentType.RUC
                ? PartnerFixtureData.completeRuc(seed.documentBase())
                : seed.documentBase();
        IdentityDocument document = documentPolicy.require(seed.documentType(), documentNumber);

        Partner partner = Partner.register(
                seed.type(),
                seed.name(),
                seed.tradeName(),
                document.type(),
                document.number(),
                seed.email(),
                seed.phone(),
                timeProvider.now()
        );

        if (seed.categoryCode() != null) {
            partner.assignCategory(categories.get(seed.categoryCode()));
        }
        if (seed.priceListCode() != null) {
            partner.assignPriceList(priceLists.get(seed.priceListCode()));
        }
        if (seed.paymentTermCode() != null) {
            partner.assignPaymentTerm(terms.get(seed.paymentTermCode()));
        }
        if (seed.creditLimit() != null) {
            partner.assignCreditLimit(Money.of(seed.creditLimit(), properties.getDefaultCurrency()));
        }

        for (PartnerFixtureData.AddressSeed address : seed.addresses()) {
            partner.addAddress(address.kind(), address.line(), address.city(), address.region(),
                    address.postalCode(), address.country(), address.primary());
        }
        for (PartnerFixtureData.ContactSeed contact : seed.contacts()) {
            partner.addContact(contact.fullName(), contact.email(), contact.phone(), contact.roleLabel());
        }
        for (String tag : seed.tags()) {
            partner.addTag(tag);
        }
        for (PartnerFixtureData.BankSeed bank : seed.banks()) {
            partner.addBankAccount(bank.bankCode(), bank.bankName(), bank.accountNumber(), bank.cci(),
                    bank.currency(), bank.holderName(), bank.purpose(), bank.primary());
        }

        partnerRepository.save(partner);
    }
}
