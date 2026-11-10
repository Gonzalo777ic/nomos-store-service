package com.nomos.store.service;

import com.valeerp.accounting.LedgerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = {"com.nomos.store.service", "com.valeerp"})
@EntityScan(basePackages = {"com.nomos.store.service", "com.valeerp"})
@EnableJpaRepositories(basePackages = {"com.nomos.store.service", "com.valeerp"})
@EnableConfigurationProperties(LedgerProperties.class)
@EnableMethodSecurity
public class NomosStoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NomosStoreServiceApplication.class, args);
    }

}
