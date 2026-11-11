package com.valeerp.partner;

public record PaymentTermRequest(String code, String name, Integer daysDue, String description) {
}
