package com.valeerp.partner;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Overrides configurables de la política documental (despliegue / compañía).
 * Prefijo: {@code valeerp.partner.document}.
 *
 * Ejemplo:
 * <pre>
 * valeerp.partner.document.ruc-checksum-enabled=true
 * valeerp.partner.document.rules.DNI.exact-length=8
 * valeerp.partner.document.rules.DNI.pattern=\d{8}
 * valeerp.partner.document.rules.PASSPORT.min-length=6
 * valeerp.partner.document.rules.PASSPORT.max-length=12
 * </pre>
 */
@ConfigurationProperties(prefix = "valeerp.partner.document")
public class PartnerDocumentPolicyProperties {

    /**
     * Activa o desactiva la verificación del dígito RUC (default true vía catálogo).
     */
    private Boolean rucChecksumEnabled;

    private Map<String, RuleOverride> rules = new LinkedHashMap<>();

    public Boolean getRucChecksumEnabled() {
        return rucChecksumEnabled;
    }

    public void setRucChecksumEnabled(Boolean rucChecksumEnabled) {
        this.rucChecksumEnabled = rucChecksumEnabled;
    }

    public Map<String, RuleOverride> getRules() {
        return rules;
    }

    public void setRules(Map<String, RuleOverride> rules) {
        this.rules = rules == null ? new LinkedHashMap<>() : rules;
    }

    public static class RuleOverride {
        private Integer exactLength;
        private Integer minLength;
        private Integer maxLength;
        private String pattern;
        private Boolean checksumEnabled;

        public Integer getExactLength() {
            return exactLength;
        }

        public void setExactLength(Integer exactLength) {
            this.exactLength = exactLength;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public void setMinLength(Integer minLength) {
            this.minLength = minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public Boolean getChecksumEnabled() {
            return checksumEnabled;
        }

        public void setChecksumEnabled(Boolean checksumEnabled) {
            this.checksumEnabled = checksumEnabled;
        }
    }
}
