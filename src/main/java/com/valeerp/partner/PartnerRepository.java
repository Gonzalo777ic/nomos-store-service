package com.valeerp.partner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Page<Partner> findAll(Pageable pageable);

    Page<Partner> findByPartnerType(PartnerType partnerType, Pageable pageable);

    Optional<Partner> findByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);

    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}
