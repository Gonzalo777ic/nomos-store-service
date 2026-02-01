package com.nomos.store.service.repository;

import com.nomos.store.service.model.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {

    List<CashMovement> findByMovementDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT cm FROM CashMovement cm WHERE cm.paymentMethod.id = :methodId AND cm.movementDate BETWEEN :start AND :end")
    List<CashMovement> findByPaymentMethodAndDateRange(
            @Param("methodId") Long methodId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}