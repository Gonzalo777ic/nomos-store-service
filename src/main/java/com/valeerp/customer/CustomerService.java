package com.valeerp.customer;

import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional
    public Customer register(CustomerController.CustomerProfile profile) {
        if (profile.email() == null || profile.email().isBlank()) {
            throw new DomainException("El cliente exige un correo");
        }
        Customer customer = profile.auth0Id() == null
                ? new Customer()
                : customerRepository.findByAuth0Id(profile.auth0Id()).orElseGet(Customer::new);
        customer.register(profile.auth0Id(), profile.email(), profile.fullName(),
                profile.documentType(), profile.documentNumber(), profile.phone(), profile.address());
        return customerRepository.save(customer);
    }

    public Optional<Customer> findByAuth(String auth0Id) {
        return customerRepository.findByAuth0Id(auth0Id);
    }

    public Customer remember(String auth0Id, String email, String name, String address) {
        Customer customer = ensure(auth0Id, email, name);
        customer.register(auth0Id, customer.getEmail(), name, customer.getDocumentType(),
                customer.getDocumentNumber(), customer.getPhone(), address);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer ensure(String auth0Id, String email, String name) {
        return customerRepository.findByAuth0Id(auth0Id).orElseGet(() -> {
            Customer customer = new Customer();
            customer.register(auth0Id, email, name, null, null, null, null);
            return customerRepository.save(customer);
        });
    }
}
