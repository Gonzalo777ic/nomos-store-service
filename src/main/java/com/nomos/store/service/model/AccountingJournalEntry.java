package com.nomos.store.service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounting_journal_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingJournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime entryDate;

    @Column(nullable = false)
    private String concept;

    @Column(name = "reference_doc")
    private String referenceDocument;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL)
    private List<AccountingJournalLine> lines;
}