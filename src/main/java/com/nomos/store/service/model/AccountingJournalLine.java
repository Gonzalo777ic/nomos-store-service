package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounting_journal_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingJournalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    @JsonIgnore // Romper ciclo JSON
    @ToString.Exclude
    private AccountingJournalEntry journalEntry;

    @Column(name = "account_code", nullable = false, length = 20)
    private String accountCode;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(nullable = false)
    @Builder.Default
    private Double debit = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double credit = 0.0;

    @Column(name = "cost_center")
    private String costCenter;
}