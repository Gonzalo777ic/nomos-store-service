package com.valeerp.pos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosReversalDocumentRepository extends JpaRepository<PosReversalDocument, Long> {

    List<PosReversalDocument> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
