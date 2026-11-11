package com.valeerp.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {
    Optional<CommissionRule> findByCode(String code);
}
