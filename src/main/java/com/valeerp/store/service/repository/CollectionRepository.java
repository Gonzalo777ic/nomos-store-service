package com.valeerp.store.service.repository;

import com.valeerp.store.service.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findBySaleId(Long saleId);

    List<Collection> findByPaymentMethodId(Long paymentMethodId);
}