package com.valeerp.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesCommissionRepository extends JpaRepository<SalesCommission, Long> {
    List<SalesCommission> findBySellerId(Long sellerId);

    List<SalesCommission> findBySaleId(Long saleId);

    List<SalesCommission> findBySaleOrderId(Long saleOrderId);
}
