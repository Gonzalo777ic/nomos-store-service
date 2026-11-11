package com.valeerp.customer;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.partner.IdentityDocument;
import com.valeerp.partner.PartnerDocumentPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TimeProvider timeProvider;
    private final PartnerDocumentPolicy documentPolicy;

    public CustomerService(CustomerRepository customerRepository,
                           TimeProvider timeProvider,
                           PartnerDocumentPolicy documentPolicy) {
        this.customerRepository = customerRepository;
        this.timeProvider = timeProvider;
        this.documentPolicy = documentPolicy;
    }

    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Customer require(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Cliente no encontrado"));
    }

    @Transactional
    public Customer register(CustomerProfileRequest profile) {
        if (profile.email() == null || profile.email().isBlank()) {
            throw new DomainException("El cliente exige un correo");
        }
        IdentityDocument document = documentPolicy.requireOptional(
                profile.documentType(), profile.documentNumber());
        Customer customer = profile.auth0Id() == null
                ? new Customer()
                : customerRepository.findByAuth0Id(profile.auth0Id()).orElseGet(Customer::new);
        customer.register(
                profile.auth0Id(),
                profile.email(),
                profile.fullName(),
                document == null ? null : document.type().name(),
                document == null ? null : document.number(),
                profile.phone(),
                profile.address()
        );
        return customerRepository.save(customer);
    }

    @Transactional
    public void retire(Long id) {
        Customer customer = require(id);
        customer.archive(Actors.currentActor(), timeProvider.now());
        customerRepository.save(customer);
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
