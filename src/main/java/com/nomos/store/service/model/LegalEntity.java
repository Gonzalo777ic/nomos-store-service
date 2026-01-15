package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "legal_entities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LegalEntityType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    @ToString.Exclude
    private LegalEntity parent;

    public String getFullDescription() {
        return legalName + " - " + taxId;
    }
}