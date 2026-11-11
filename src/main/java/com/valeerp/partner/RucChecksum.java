package com.valeerp.partner;

/**
 * Dígito verificador de RUC (algoritmo módulo 11 SUNAT).
 * Factores de ponderación como constante de dominio, no literal suelto en servicios HTTP.
 */
public final class RucChecksum {

    private static final int[] WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
    private static final int MODULUS = 11;

    private RucChecksum() {
    }

    public static boolean matches(String rucDigits) {
        if (rucDigits == null || rucDigits.length() != DocumentRuleCatalog.RUC_LENGTH) {
            return false;
        }
        int expected = expectedCheckDigit(rucDigits.substring(0, DocumentRuleCatalog.RUC_LENGTH - 1));
        int actual = Character.digit(rucDigits.charAt(DocumentRuleCatalog.RUC_LENGTH - 1), 10);
        return expected == actual;
    }

    public static int expectedCheckDigit(String firstTenDigits) {
        if (firstTenDigits == null || firstTenDigits.length() != DocumentRuleCatalog.RUC_LENGTH - 1) {
            throw new IllegalArgumentException("RUC base inválida");
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            int digit = Character.digit(firstTenDigits.charAt(i), 10);
            if (digit < 0) {
                throw new IllegalArgumentException("RUC no numérico");
            }
            sum += digit * WEIGHTS[i];
        }
        int remainder = MODULUS - (sum % MODULUS);
        if (remainder == 10) {
            return 0;
        }
        if (remainder == MODULUS) {
            return 1;
        }
        return remainder;
    }
}
