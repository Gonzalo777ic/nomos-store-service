package com.nomos.store.service.repository;

import com.nomos.store.service.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findBySellerId(Long sellerId);
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}