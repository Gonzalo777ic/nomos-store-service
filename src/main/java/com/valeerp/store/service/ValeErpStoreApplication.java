package com.valeerp.store.service;

import com.valeerp.accounting.LedgerProperties;
import com.valeerp.partner.PartnerDocumentPolicyProperties;
import com.valeerp.partner.PartnerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = {"com.valeerp.store.service", "com.valeerp"})
@EntityScan(basePackages = {"com.valeerp.store.service", "com.valeerp"})
@EnableJpaRepositories(basePackages = {"com.valeerp.store.service", "com.valeerp"})
@EnableConfigurationProperties({
        LedgerProperties.class,
        PartnerProperties.class,
        PartnerDocumentPolicyProperties.class
})
@EnableMethodSecurity
public class ValeErpStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValeErpStoreApplication.class, args);
    }

}
