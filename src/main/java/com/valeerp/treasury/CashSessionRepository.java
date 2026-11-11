package com.valeerp.treasury;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashSessionRepository extends JpaRepository<CashSession, Long> {

    Page<CashSession> findByCashRegisterId(Long cashRegisterId, Pageable pageable);

    Optional<CashSession> findByCashRegisterIdAndStatus(Long cashRegisterId, CashSessionStatus status);

    boolean existsByCashRegisterIdAndStatus(Long cashRegisterId, CashSessionStatus status);
}
