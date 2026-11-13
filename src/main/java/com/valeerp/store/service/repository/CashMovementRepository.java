package com.valeerp.store.service.repository;

import com.valeerp.store.service.model.CashMovement;
import com.valeerp.store.service.model.CashMovementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {

    List<CashMovement> findByMovementDateBetween(LocalDateTime start, LocalDateTime end);

    List<CashMovement> findByCashSessionIdAndStatus(Long cashSessionId, CashMovementStatus status);

    List<CashMovement> findByOccurredAtBetween(Instant start, Instant end);

    @Query("SELECT cm FROM CashMovement cm WHERE cm.paymentMethod.id = :methodId AND cm.movementDate BETWEEN :start AND :end")
    List<CashMovement> findByPaymentMethodAndDateRange(
            @Param("methodId") Long methodId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    boolean existsByExternalReference(String externalReference);
}
