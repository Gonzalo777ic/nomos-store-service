package com.nomos.store.service.repository;

import com.nomos.store.service.model.StoreSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Optional;
import java.util.List;

@Repository
public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {

    Optional<StoreSchedule> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<StoreSchedule> findAll();
}