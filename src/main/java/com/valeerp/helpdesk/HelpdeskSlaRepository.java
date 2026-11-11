package com.valeerp.helpdesk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HelpdeskSlaRepository extends JpaRepository<HelpdeskSla, Long> {

    Page<HelpdeskSla> findAll(Pageable pageable);

    Optional<HelpdeskSla> findByCode(String code);
}
