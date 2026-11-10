package com.valeerp.sales;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PenaltyPolicy {

    private static double monthlyRate = 0.009;

    public PenaltyPolicy(@Value("${valeerp.collections.monthly-penalty-rate}") double monthlyRate) {
        PenaltyPolicy.monthlyRate = monthlyRate;
    }

    public static double monthlyRate() {
        return monthlyRate;
    }
}
