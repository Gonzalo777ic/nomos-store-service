package com.valeerp.accounting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByCompanyIdOrderByPaymentDateDescIdDesc(Long companyId);

    List<Payment> findByCompanyIdAndPartnerId(Long companyId, Long partnerId);
}
