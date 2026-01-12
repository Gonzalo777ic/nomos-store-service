package com.nomos.store.service.repository;

import com.nomos.store.service.model.AccountsReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AccountsReceivableRepository extends JpaRepository<AccountsReceivable, Long> {
    Optional<AccountsReceivable> findBySaleId(Long saleId);

    List<AccountsReceivable> findByStatus(String status);
}