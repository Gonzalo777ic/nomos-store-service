package com.nomos.store.service.repository;

import com.nomos.store.service.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findBySaleId(Long saleId);

    List<Collection> findByPaymentMethodId(Long paymentMethodId);
}