package com.valeerp.pos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosTicketRepository extends JpaRepository<PosTicket, Long> {

    Page<PosTicket> findByPosConfigId(Long posConfigId, Pageable pageable);

    Page<PosTicket> findByCompanyId(Long companyId, Pageable pageable);

    Page<PosTicket> findByStatus(PosTicketStatus status, Pageable pageable);
}
