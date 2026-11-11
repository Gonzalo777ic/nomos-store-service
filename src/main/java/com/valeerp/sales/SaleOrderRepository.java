package com.valeerp.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {

    Page<SaleOrder> findByPartnerId(Long partnerId, Pageable pageable);

    Page<SaleOrder> findByStatus(SaleOrderStatus status, Pageable pageable);

    Page<SaleOrder> findByPartnerIdAndStatus(Long partnerId, SaleOrderStatus status, Pageable pageable);

    Page<SaleOrder> findByOpportunityId(Long opportunityId, Pageable pageable);

    @Query("select max(d.number) from SaleOrderDocument d where d.series = :series")
    Optional<String> findMaxCommercialDocumentNumber(@Param("series") String series);
}
