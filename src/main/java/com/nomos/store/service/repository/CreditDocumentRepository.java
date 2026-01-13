package com.nomos.store.service.repository;

import com.nomos.store.service.model.CreditDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditDocumentRepository extends JpaRepository<CreditDocument, Long> {
    List<CreditDocument> findByAccountsReceivableId(Long accountsReceivableId);
}