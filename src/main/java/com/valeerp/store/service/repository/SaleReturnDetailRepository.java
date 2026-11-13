package com.valeerp.store.service.repository;

import com.valeerp.store.service.model.SaleReturnDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleReturnDetailRepository extends JpaRepository<SaleReturnDetail, Long> {
}