package com.valeerp.treasury;

import com.valeerp.kernel.Currency;

/**
 * Política local de Caja / Tesorería. Moneda y límites viven aquí, no en controladores.
 */
public final class TreasuryPolicy {

    public static final Currency DEFAULT_CURRENCY = Currency.PEN;

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int CODE_MAX_LENGTH = 32;
    public static final int NAME_MAX_LENGTH = 120;
    public static final int CONCEPT_MAX_LENGTH = 400;
    public static final int REFERENCE_MAX_LENGTH = 120;
    public static final int DIFFERENCE_NOTE_MAX_LENGTH = 500;

    public static final String SOURCE_COLLECTION = "COLLECTION";
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_SESSION_CLOSE = "CASH_SESSION_CLOSE";
    public static final String SOURCE_MASS_PAYMENT = "MASS_VENDOR_PAYMENT";
    public static final String SOURCE_CASH_BANK_TRANSFER = "CASH_BANK_TRANSFER";

    private TreasuryPolicy() {
    }
}
