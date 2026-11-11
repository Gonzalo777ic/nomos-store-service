package com.valeerp.partner;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Regla de formato de un tipo documental. Longitudes y patrón viven aquí,
 * no en controladores ni DTOs HTTP.
 */
public final class DocumentFormatRule {

    private final DocumentType type;
    private final int minLength;
    private final int maxLength;
    private final Pattern pattern;
    private final boolean checksumRequired;

    public DocumentFormatRule(DocumentType type, int minLength, int maxLength,
                              String pattern, boolean checksumRequired) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }
        if (minLength < 1 || maxLength < minLength) {
            throw new IllegalArgumentException("longitud inválida para " + type);
        }
        this.type = type;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.pattern = Pattern.compile(Objects.requireNonNull(pattern, "pattern"));
        this.checksumRequired = checksumRequired;
    }

    public static DocumentFormatRule exact(DocumentType type, int length, String pattern, boolean checksum) {
        return new DocumentFormatRule(type, length, length, pattern, checksum);
    }

    public static DocumentFormatRule range(DocumentType type, int min, int max, String pattern) {
        return new DocumentFormatRule(type, min, max, pattern, false);
    }

    public DocumentType type() {
        return type;
    }

    public int minLength() {
        return minLength;
    }

    public int maxLength() {
        return maxLength;
    }

    public Pattern pattern() {
        return pattern;
    }

    public boolean checksumRequired() {
        return checksumRequired;
    }

    public DocumentFormatRule withChecksum(boolean enabled) {
        return new DocumentFormatRule(type, minLength, maxLength, pattern.pattern(), enabled);
    }

    public DocumentFormatRule withLengths(Integer min, Integer max) {
        int resolvedMin = min != null ? min : minLength;
        int resolvedMax = max != null ? max : maxLength;
        return new DocumentFormatRule(type, resolvedMin, resolvedMax, pattern.pattern(), checksumRequired);
    }

    public DocumentFormatRule withPattern(String newPattern) {
        if (newPattern == null || newPattern.isBlank()) {
            return this;
        }
        return new DocumentFormatRule(type, minLength, maxLength, newPattern, checksumRequired);
    }
}
