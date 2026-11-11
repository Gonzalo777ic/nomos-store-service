package com.valeerp.treasury;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MassVendorPaymentRepository extends JpaRepository<MassVendorPayment, Long> {

    Page<MassVendorPayment> findByCompanyId(Long companyId, Pageable pageable);
}
