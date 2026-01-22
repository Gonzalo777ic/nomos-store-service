package com.nomos.store.service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounting_journal_lines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingJournalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journal_entry_id")
    private AccountingJournalEntry journalEntry;

    @Column(nullable = false)
    private String accountCode;

    @Column(nullable = false)
    private String accountName;

    private Double debit;  // Debe
    private Double credit; // Haber
}