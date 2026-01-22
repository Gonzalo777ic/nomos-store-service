package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "reference_doc", length = 50)
    private String referenceDocument;

    @Builder.Default
    @Column(nullable = false)
    private String status = "POSTED";

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("journalEntry")
    @Builder.Default
    private List<AccountingJournalLine> lines = new ArrayList<>();


    public boolean isBalanced() {
        double totalDebit = lines.stream().mapToDouble(AccountingJournalLine::getDebit).sum();
        double totalCredit = lines.stream().mapToDouble(AccountingJournalLine::getCredit).sum();
        return Math.abs(totalDebit - totalCredit) < 0.001;
    }
}