package com.valeerp.sales;

import com.valeerp.kernel.Currency;

/**
 * Política de dominio del contexto Ventas.
 * Tasas fiscales viven en el catálogo persistido ({@code TaxRate}); nunca un literal 1.18 / 0.18.
 */
public final class SalesPolicy {

    public static final Currency DEFAULT_CURRENCY = Currency.PEN;

    /** Código/nombre de búsqueda del impuesto vigente en el catálogo fiscal local. */
    public static final String DEFAULT_TAX_CATALOG_CODE = "IGV";

    public static final String SALE_ORDER_DOCUMENT_SERIES = "SO";

    public static final int DEFAULT_QUOTATION_VALIDITY_DAYS = 15;

    /** Compatibilidad con promociones legacy del flujo Sale POS/tienda. */
    public static final String PROMOTION_PERCENT = "PORCENTAJE";
    public static final String PROMOTION_FIXED = "MONTO_FIJO";
    public static final String PROMOTION_THREE_FOR_TWO = "TRES_POR_DOS";
    public static final String PROMOTION_TWO_FOR_ONE = "DOS_POR_UNO";

    private SalesPolicy() {
    }
}
