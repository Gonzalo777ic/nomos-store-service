package com.nomos.store.service.repository;

import com.nomos.store.service.model.AccountingJournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountingJournalEntryRepository extends JpaRepository<AccountingJournalEntry, Long> {

    List<AccountingJournalEntry> findByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AccountingJournalEntry> findByReferenceDocument(String referenceDocument);

    List<AccountingJournalEntry> findAllByOrderByEntryDateDesc();
}