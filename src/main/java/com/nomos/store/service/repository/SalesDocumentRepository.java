package com.nomos.store.service.repository;

import com.nomos.store.service.model.SalesDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesDocumentRepository extends JpaRepository<SalesDocument, Long> {

    List<SalesDocument> findBySaleId(Long saleId);

    Optional<SalesDocument> findBySeriesAndNumber(String series, String number);

    long countBySeries(String series);
}