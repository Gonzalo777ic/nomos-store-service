package com.valeerp.treasury;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashBankTransferRepository extends JpaRepository<CashBankTransfer, Long> {

    Page<CashBankTransfer> findByCompanyId(Long companyId, Pageable pageable);
}
