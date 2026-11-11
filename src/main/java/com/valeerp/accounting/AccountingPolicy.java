package com.valeerp.accounting;

import com.valeerp.kernel.Currency;

/**
 * Política local de contabilidad. Códigos de diario por defecto y moneda viven aquí.
 */
public final class AccountingPolicy {

    public static final Currency DEFAULT_CURRENCY = Currency.PEN;

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int ACCOUNT_CODE_MAX_LENGTH = 32;
    public static final int ACCOUNT_NAME_MAX_LENGTH = 200;
    public static final int JOURNAL_CODE_MAX_LENGTH = 16;
    public static final int JOURNAL_NAME_MAX_LENGTH = 120;

    private AccountingPolicy() {
    }
}
