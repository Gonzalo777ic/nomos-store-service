package com.nomos.store.service.repository;

import com.nomos.store.service.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
    List<Installment> findByAccountsReceivableId(Long arId);
}