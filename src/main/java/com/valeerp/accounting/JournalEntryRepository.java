package com.valeerp.accounting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    Page<JournalEntry> findByCompanyIdOrderByEntryDateDescIdDesc(Long companyId, Pageable pageable);

    List<JournalEntry> findByReferenceDocumentOrderByIdAsc(String referenceDocument);

    List<JournalEntry> findByCompanyIdAndReferenceDocument(Long companyId, String referenceDocument);

    @Query("""
            select e from JournalEntry e
            where e.companyId = :companyId
              and e.status in (com.valeerp.accounting.JournalEntryStatus.POSTED,
                               com.valeerp.accounting.JournalEntryStatus.REVERSED)
              and e.entryDate >= :fromDate
              and e.entryDate <= :toDate
            order by e.entryDate asc, e.id asc
            """)
    List<JournalEntry> findPostedBetween(@Param("companyId") Long companyId,
                                         @Param("fromDate") LocalDate fromDate,
                                         @Param("toDate") LocalDate toDate);

    @Query("""
            select e from JournalEntry e
            where e.companyId = :companyId
              and e.status in (com.valeerp.accounting.JournalEntryStatus.POSTED,
                               com.valeerp.accounting.JournalEntryStatus.REVERSED)
              and e.entryDate <= :asOf
            order by e.entryDate asc, e.id asc
            """)
    List<JournalEntry> findPostedUpTo(@Param("companyId") Long companyId,
                                      @Param("asOf") LocalDate asOf);
}
