package com.valeerp.kernel;

public class DomainException extends RuntimeException {

    public static final String DEFAULT_CODE = "DOMAIN_ERROR";

    private final String code;

    public DomainException(String message) {
        this(DEFAULT_CODE, message);
    }

    public DomainException(String code, String message) {
        super(message);
        this.code = code == null || code.isBlank() ? DEFAULT_CODE : code;
    }

    public String code() {
        return code;
    }
}
