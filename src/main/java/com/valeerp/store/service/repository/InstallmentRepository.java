package com.valeerp.store.service.repository;

import com.valeerp.store.service.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
    List<Installment> findByAccountsReceivableId(Long arId);
}