package com.valeerp.partner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RucChecksumTest {

    @Test
    void validatesKnownRuc() {
        assertTrue(RucChecksum.matches("20100070970"));
        assertTrue(RucChecksum.matches("20123456786"));
    }

    @Test
    void rejectsWrongCheckDigit() {
        assertFalse(RucChecksum.matches("20100070971"));
    }

    @Test
    void computesExpectedDigit() {
        assertEquals(0, RucChecksum.expectedCheckDigit("2010007097"));
        assertEquals(6, RucChecksum.expectedCheckDigit("2012345678"));
    }
}
