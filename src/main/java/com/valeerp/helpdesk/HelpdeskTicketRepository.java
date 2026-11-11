package com.valeerp.helpdesk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpdeskTicketRepository extends JpaRepository<HelpdeskTicket, Long> {

    Page<HelpdeskTicket> findAll(Pageable pageable);

    Page<HelpdeskTicket> findByStatus(TicketStatus status, Pageable pageable);

    Page<HelpdeskTicket> findByPartnerId(Long partnerId, Pageable pageable);

    Page<HelpdeskTicket> findByAssigneeUserId(Long assigneeUserId, Pageable pageable);
}
