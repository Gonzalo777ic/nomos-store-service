package com.valeerp.accounting;

import java.time.LocalDate;

public record VoidEntryRequest(LocalDate reversalDate) {
}
