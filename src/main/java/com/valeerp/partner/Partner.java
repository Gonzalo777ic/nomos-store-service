package com.valeerp.partner;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Maestro comercial Contactos / Partners. Separado del User de identidad.
 * Baja lógica en la raíz; hijos se archivan, nunca delete físico.
 */
@Entity
@Table(name = "partners")
public class Partner extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_type", nullable = false, length = 32)
    private PartnerType partnerType;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 32)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 64)
    private String documentNumber;

    @Column(length = 120)
    private String email;

    @Column(length = 60)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PartnerCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_ref_id")
    private LocalPriceListRef priceListRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id")
    private PaymentTerm paymentTerm;

    @Column(name = "credit_limit_amount", precision = 19, scale = Money.SCALE)
    private BigDecimal creditLimitAmount;

    @Column(name = "credit_limit_currency", length = 3)
    private String creditLimitCurrency;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<PartnerAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<PartnerContact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<PartnerTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<PartnerBankAccount> bankAccounts = new ArrayList<>();

    protected Partner() {
    }

    public static Partner register(PartnerType type, String name, String tradeName,
                                   DocumentType documentType, String documentNumber,
                                   String email, String phone, Instant createdAt) {
        if (type == null) {
            throw new DomainException("El tipo de partner es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El partner exige un nombre");
        }
        if (documentType == null) {
            throw new DomainException("El tipo de documento es obligatorio");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new DomainException("El número de documento es obligatorio");
        }
        Partner partner = new Partner();
        partner.partnerType = type;
        partner.name = name.trim();
        partner.tradeName = blankToNull(tradeName);
        partner.documentType = documentType;
        partner.documentNumber = documentNumber.trim();
        partner.email = blankToNull(email);
        partner.phone = blankToNull(phone);
        partner.createdAt = createdAt;
        return partner;
    }

    public void rename(String name, String tradeName) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El partner exige un nombre");
        }
        this.name = name.trim();
        this.tradeName = blankToNull(tradeName);
    }

    public void changeDocument(DocumentType documentType, String documentNumber) {
        if (documentType == null) {
            throw new DomainException("El tipo de documento es obligatorio");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new DomainException("El número de documento es obligatorio");
        }
        this.documentType = documentType;
        this.documentNumber = documentNumber.trim();
    }

    public void changeContactChannels(String email, String phone) {
        this.email = blankToNull(email);
        this.phone = blankToNull(phone);
    }

    public void changeType(PartnerType type) {
        if (type == null) {
            throw new DomainException("El tipo de partner es obligatorio");
        }
        this.partnerType = type;
    }

    public PartnerAddress addAddress(AddressKind kind, String line, String city, String region,
                                     String postalCode, String country, boolean primary) {
        if (primary) {
            clearPrimaryAddresses();
        }
        PartnerAddress address = PartnerAddress.create(this, kind, line, city, region, postalCode, country, primary);
        addresses.add(address);
        return address;
    }

    public PartnerContact addContact(String fullName, String email, String phone, String roleLabel) {
        PartnerContact contact = PartnerContact.create(this, fullName, email, phone, roleLabel);
        contacts.add(contact);
        return contact;
    }

    public PartnerTag addTag(String label) {
        String normalized = label == null ? "" : label.trim();
        boolean exists = tags.stream()
                .filter(tag -> !tag.isRetired())
                .anyMatch(tag -> tag.sameLabel(normalized));
        if (exists) {
            throw new DomainException("La etiqueta ya está asignada: " + normalized);
        }
        PartnerTag tag = PartnerTag.create(this, normalized);
        tags.add(tag);
        return tag;
    }

    public void assignCategory(PartnerCategory category) {
        this.category = category;
    }

    public void assignPriceList(LocalPriceListRef priceListRef) {
        this.priceListRef = priceListRef;
    }

    public void assignPaymentTerm(PaymentTerm paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    /**
     * Límite de crédito comercial con {@link Money}. Persistido como amount+currency
     * (Money del store aún no es {@code @Embeddable}; alinear con inventory cuando se unifique).
     */
    public void assignCreditLimit(Money creditLimit) {
        if (creditLimit == null) {
            this.creditLimitAmount = null;
            this.creditLimitCurrency = null;
            return;
        }
        if (creditLimit.isNegative()) {
            throw new DomainException("El límite de crédito no puede ser negativo");
        }
        this.creditLimitAmount = creditLimit.amount();
        this.creditLimitCurrency = creditLimit.currency();
    }

    public PartnerBankAccount addBankAccount(String bankCode, String bankName, String accountNumber,
                                             String cci, String currencyCode, String holderName,
                                             BankAccountPurpose purpose, boolean primary) {
        if (primary) {
            clearPrimaryBankAccounts();
        }
        PartnerBankAccount account = PartnerBankAccount.create(
                this, bankCode, bankName, accountNumber, cci, currencyCode, holderName, purpose, primary);
        bankAccounts.add(account);
        return account;
    }

    public void retireAddress(Long addressId, Actor actor, Instant when) {
        PartnerAddress address = requireAddress(addressId);
        address.retire(actor, when);
    }

    public void retireContact(Long contactId, Actor actor, Instant when) {
        PartnerContact contact = requireContact(contactId);
        contact.retire(actor, when);
    }

    public void retireTag(Long tagId, Actor actor, Instant when) {
        PartnerTag tag = requireTag(tagId);
        tag.retire(actor, when);
    }

    public void retireBankAccount(Long bankAccountId, Actor actor, Instant when) {
        PartnerBankAccount account = requireBankAccount(bankAccountId);
        account.retire(actor, when);
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
        for (PartnerAddress address : addresses) {
            if (!address.isRetired()) {
                address.retire(actor, when);
            }
        }
        for (PartnerContact contact : contacts) {
            if (!contact.isRetired()) {
                contact.retire(actor, when);
            }
        }
        for (PartnerTag tag : tags) {
            if (!tag.isRetired()) {
                tag.retire(actor, when);
            }
        }
        for (PartnerBankAccount account : bankAccounts) {
            if (!account.isRetired()) {
                account.retire(actor, when);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public PartnerType getPartnerType() {
        return partnerType;
    }

    public String getName() {
        return name;
    }

    public String getTradeName() {
        return tradeName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public PartnerCategory getCategory() {
        return category;
    }

    public LocalPriceListRef getPriceListRef() {
        return priceListRef;
    }

    public PaymentTerm getPaymentTerm() {
        return paymentTerm;
    }

    public Money creditLimit() {
        if (creditLimitAmount == null || creditLimitCurrency == null || creditLimitCurrency.isBlank()) {
            return null;
        }
        return Money.of(creditLimitAmount, creditLimitCurrency);
    }

    public List<PartnerAddress> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public List<PartnerContact> getContacts() {
        return Collections.unmodifiableList(contacts);
    }

    public List<PartnerTag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public List<PartnerBankAccount> getBankAccounts() {
        return Collections.unmodifiableList(bankAccounts);
    }

    public List<PartnerAddress> activeAddresses() {
        return addresses.stream().filter(a -> !a.isRetired()).toList();
    }

    public List<PartnerContact> activeContacts() {
        return contacts.stream().filter(c -> !c.isRetired()).toList();
    }

    public List<PartnerTag> activeTags() {
        return tags.stream().filter(t -> !t.isRetired()).toList();
    }

    public List<PartnerBankAccount> activeBankAccounts() {
        return bankAccounts.stream().filter(a -> !a.isRetired()).toList();
    }

    private PartnerAddress requireAddress(Long addressId) {
        return addresses.stream()
                .filter(a -> !a.isRetired() && addressId.equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Dirección no encontrada"));
    }

    private PartnerContact requireContact(Long contactId) {
        return contacts.stream()
                .filter(c -> !c.isRetired() && contactId.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Contacto hijo no encontrado"));
    }

    private PartnerTag requireTag(Long tagId) {
        return tags.stream()
                .filter(t -> !t.isRetired() && tagId.equals(t.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Etiqueta no encontrada"));
    }

    private PartnerBankAccount requireBankAccount(Long bankAccountId) {
        return bankAccounts.stream()
                .filter(a -> !a.isRetired() && bankAccountId.equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Cuenta bancaria no encontrada"));
    }

    private void clearPrimaryAddresses() {
        for (PartnerAddress address : addresses) {
            if (!address.isRetired() && address.isPrimaryAddress()) {
                address.markPrimary(false);
            }
        }
    }

    private void clearPrimaryBankAccounts() {
        for (PartnerBankAccount account : bankAccounts) {
            if (!account.isRetired() && account.isPrimaryAccount()) {
                account.markPrimary(false);
            }
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Partner partner)) {
            return false;
        }
        return id != null && id.equals(partner.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Partner{" + partnerType + " " + documentType + ":" + documentNumber + "}";
    }

    static PartnerType requireType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de partner es obligatorio");
        }
        try {
            return PartnerType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de partner no soportado: " + raw);
        }
    }
}
