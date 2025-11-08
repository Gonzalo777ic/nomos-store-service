package com.nomos.store.service.repository;

import com.nomos.store.service.model.SaleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {

    /**
     * Obtener todos los detalles de venta asociados a un ID de venta específico (saleId).
     */
    List<SaleDetail> findBySaleId(Long saleId);
}