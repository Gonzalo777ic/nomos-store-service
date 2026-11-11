package com.valeerp.partner;

import java.util.Objects;

/**
 * Value object: tipo + número documental ya normalizado y validado por política.
 */
public final class IdentityDocument {

    private final DocumentType type;
    private final String number;

    private IdentityDocument(DocumentType type, String number) {
        this.type = Objects.requireNonNull(type, "type");
        this.number = Objects.requireNonNull(number, "number");
    }

    static IdentityDocument ofValidated(DocumentType type, String normalizedNumber) {
        return new IdentityDocument(type, normalizedNumber);
    }

    public DocumentType type() {
        return type;
    }

    public String number() {
        return number;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IdentityDocument that)) {
            return false;
        }
        return type == that.type && number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }

    @Override
    public String toString() {
        return type.name() + ":" + number;
    }
}
