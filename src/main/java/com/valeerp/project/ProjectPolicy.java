package com.valeerp.project;

import com.valeerp.kernel.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Política local del contexto Proyectos. Horas y moneda viven aquí, no en controladores.
 */
public final class ProjectPolicy {

    public static final Currency DEFAULT_CURRENCY = Currency.PEN;

    public static final int HOURS_SCALE = 2;

    public static final RoundingMode HOURS_ROUNDING = RoundingMode.HALF_UP;

    public static final BigDecimal MIN_TIMESHEET_HOURS = new BigDecimal("0.25");

    private ProjectPolicy() {
    }

    public static BigDecimal normalizeHours(BigDecimal hours) {
        if (hours == null) {
            return BigDecimal.ZERO.setScale(HOURS_SCALE, HOURS_ROUNDING);
        }
        return hours.setScale(HOURS_SCALE, HOURS_ROUNDING);
    }
}
