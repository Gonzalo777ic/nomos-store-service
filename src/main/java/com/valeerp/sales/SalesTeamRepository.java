package com.valeerp.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesTeamRepository extends JpaRepository<SalesTeam, Long> {
    Optional<SalesTeam> findByCode(String code);
}
