package com.nomos.store.service.repository;

import com.nomos.store.service.model.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {

    // Se elimina el método findActiveRateByDate y findActiveRateNow

    // Podemos añadir un método útil para buscar por nombre si es necesario
    List<TaxRate> findByName(String name);
}