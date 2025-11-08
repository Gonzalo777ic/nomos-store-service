package com.nomos.store.service.repository;

import com.nomos.store.service.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Ejemplo de método de consulta útil: Obtener ventas por un vendedor específico
    List<Sale> findBySellerId(Long sellerId);

    // Ejemplo de método de consulta útil: Obtener ventas dentro de un rango de fechas
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}