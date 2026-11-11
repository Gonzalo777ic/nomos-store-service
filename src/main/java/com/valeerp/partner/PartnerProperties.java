package com.valeerp.partner;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del módulo Partner / Contactos.
 * Prefijo: {@code valeerp.partner}.
 */
@ConfigurationProperties(prefix = "valeerp.partner")
public class PartnerProperties {

    /**
     * Si true, {@link PartnerFixtureRunner} siembra catálogos y partners de demo.
     */
    private boolean seedEnabled = false;

    private String defaultCurrency = PartnerCommercePolicy.DEFAULT_CURRENCY;

    public boolean isSeedEnabled() {
        return seedEnabled;
    }

    public void setSeedEnabled(boolean seedEnabled) {
        this.seedEnabled = seedEnabled;
    }

    public String getDefaultCurrency() {
        return defaultCurrency == null || defaultCurrency.isBlank()
                ? PartnerCommercePolicy.DEFAULT_CURRENCY
                : defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
}
