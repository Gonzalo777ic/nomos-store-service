package com.valeerp.accounting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceDocumentRepository extends JpaRepository<InvoiceDocument, Long> {

    Page<InvoiceDocument> findByCompanyIdAndSide(Long companyId, InvoiceSide side, Pageable pageable);

    Page<InvoiceDocument> findByCompanyId(Long companyId, Pageable pageable);

    List<InvoiceDocument> findByIdIn(Collection<Long> ids);

    Optional<InvoiceDocument> findByCompanyIdAndDocumentNumberAndSide(
            Long companyId, String documentNumber, InvoiceSide side);
}
