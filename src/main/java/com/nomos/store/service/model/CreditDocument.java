package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "credit_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_receivable_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private AccountsReceivable accountsReceivable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditDocumentType type;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;


    @Column(name = "debtor_name")
    private String debtorName;

    @Column(name = "debtor_id_number")
    private String debtorIdNumber;

    @Column(name = "creditor_name")
    private String creditorName;

    @Column(name = "guarantor_name")
    private String guarantorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditDocumentStatus status;

    @Column(columnDefinition = "TEXT")
    private String legalNotes;


    @Column(name = "place_of_issue")
    private String placeOfIssue;

    @Column(name = "place_of_payment")
    private String placeOfPayment;

}