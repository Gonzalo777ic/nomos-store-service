package com.nomos.store.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class NomosStoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NomosStoreServiceApplication.class, args);
    }

}
