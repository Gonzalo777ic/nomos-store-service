package com.valeerp.pos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosReturnRepository extends JpaRepository<PosReturn, Long> {

    List<PosReturn> findByTicketId(Long ticketId);

    Page<PosReturn> findByTicketId(Long ticketId, Pageable pageable);
}
