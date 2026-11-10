package com.valeerp.customer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> list() {
        return customerService.findAll();
    }

    public record CustomerProfile(
            String auth0Id,
            String email,
            String fullName,
            String documentType,
            String documentNumber,
            String phone,
            String address
    ) {
    }

    @PostMapping
    public Customer register(@RequestBody CustomerProfile profile) {
        return customerService.register(profile);
    }

    @GetMapping("/document-types")
    public List<DocumentTypeView> documentTypes() {
        return Arrays.stream(DocumentType.values())
                .map(type -> new DocumentTypeView(type.name(), type.description()))
                .toList();
    }

    public enum DocumentType {
        DNI("Documento Nacional de Identidad"),
        RUC("Registro Único de Contribuyentes"),
        PASSPORT("Pasaporte"),
        CE("Carné de Extranjería");

        private final String description;

        DocumentType(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }

    public record DocumentTypeView(String key, String description) {
    }
}
