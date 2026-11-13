package com.valeerp.store.service.repository;

import com.valeerp.store.service.model.SaleReturn;
import com.valeerp.store.service.model.SaleReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {

    List<SaleReturn> findBySaleId(Long saleId);

    List<SaleReturn> findByStatus(SaleReturnStatus status);
}