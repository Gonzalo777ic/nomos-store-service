package com.valeerp.partner;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
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
import jakarta.persistence.Table;

/**
 * Cuenta bancaria del partner para cobranza/pago. Maestro con soft-delete.
 */
@Entity
@Table(name = "partner_bank_accounts")
public class PartnerBankAccount extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 120)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 64)
    private String accountNumber;

    @Column(length = 32)
    private String cci;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "holder_name", length = 200)
    private String holderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BankAccountPurpose purpose;

    @Column(name = "primary_account", nullable = false)
    private boolean primaryAccount;

    protected PartnerBankAccount() {
    }

    static PartnerBankAccount create(Partner partner, String bankCode, String bankName, String accountNumber,
                                     String cci, String currencyCode, String holderName,
                                     BankAccountPurpose purpose, boolean primary) {
        if (partner == null) {
            throw new DomainException("La cuenta exige un partner");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new DomainException("El banco es obligatorio");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new DomainException("El número de cuenta es obligatorio");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new DomainException("La moneda de la cuenta es obligatoria");
        }
        if (purpose == null) {
            throw new DomainException("El propósito de la cuenta es obligatorio");
        }
        PartnerBankAccount account = new PartnerBankAccount();
        account.partner = partner;
        account.bankCode = blankToNull(bankCode);
        account.bankName = bankName.trim();
        account.accountNumber = accountNumber.trim();
        account.cci = blankToNull(cci);
        account.currencyCode = currencyCode.trim().toUpperCase();
        account.holderName = blankToNull(holderName);
        account.purpose = purpose;
        account.primaryAccount = primary;
        return account;
    }

    void markPrimary(boolean primary) {
        this.primaryAccount = primary;
    }

    public Long getId() {
        return id;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCci() {
        return cci;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getHolderName() {
        return holderName;
    }

    public BankAccountPurpose getPurpose() {
        return purpose;
    }

    public boolean isPrimaryAccount() {
        return primaryAccount;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PartnerBankAccount account)) {
            return false;
        }
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
