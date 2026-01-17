package com.nomos.store.service.repository;

import com.nomos.store.service.model.SaleReturn;
import com.nomos.store.service.model.SaleReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {

    List<SaleReturn> findBySaleId(Long saleId);

    List<SaleReturn> findByStatus(SaleReturnStatus status);
}