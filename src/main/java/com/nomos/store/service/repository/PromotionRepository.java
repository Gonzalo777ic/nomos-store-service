package com.nomos.store.service.repository;

import com.nomos.store.service.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Busca promociones que estén activas y dentro del rango de fechas actual.
     */
    List<Promotion> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(
            LocalDateTime currentDateTime1, LocalDateTime currentDateTime2);

    /**
     * Busca promociones por el tipo de aplicación (e.g., para calcular el total de la venta).
     */
    List<Promotion> findByAppliesToAndIsActiveTrue(String appliesTo);
}