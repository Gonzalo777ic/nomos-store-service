package com.valeerp.store.service.repository;

import com.valeerp.store.service.model.PromotionTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionTargetRepository extends JpaRepository<PromotionTarget, Long> {

    /**
     * Encuentra todos los objetivos de una promoción específica
     */
    List<PromotionTarget> findByPromotionId(Long promotionId);

    /**
     * Elimina todos los objetivos de una promoción
     */
    void deleteByPromotionId(Long promotionId);
}