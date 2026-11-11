package com.valeerp.payable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, Long> {

    Page<AccountsPayable> findByCompanyId(Long companyId, Pageable pageable);

    Optional<AccountsPayable> findBySupplierInvoiceId(Long supplierInvoiceId);
}
