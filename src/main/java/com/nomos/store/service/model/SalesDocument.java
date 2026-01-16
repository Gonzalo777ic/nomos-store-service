package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnore // Evitar ciclos infinitos al serializar
    @ToString.Exclude
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SalesDocumentType type;

    @Column(nullable = false, length = 4)
    private String series;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SalesDocumentStatus status;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;


    @Column(name = "digest_value")
    private String digestValue;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "xml_url")
    private String xmlUrl;

    @Column(name = "cdr_url")
    private String cdrUrl; // Constancia de Recepción

    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;

    public String getFullDocumentId() {
        return series + "-" + number;
    }
}