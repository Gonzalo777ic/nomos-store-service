package com.nomos.store.service.repository;

import com.nomos.store.service.model.StoreScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreScheduleExceptionRepository extends JpaRepository<StoreScheduleException, Long> {

    Optional<StoreScheduleException> findByDate(LocalDate date);

    List<StoreScheduleException> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);

    boolean existsByDate(LocalDate date);
}