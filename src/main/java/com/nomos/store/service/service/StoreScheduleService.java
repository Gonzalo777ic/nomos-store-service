package com.nomos.store.service.service;

import com.nomos.store.service.model.StoreSchedule;
import com.nomos.store.service.repository.StoreScheduleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreScheduleService {

    private final StoreScheduleRepository repository;

    /**
     * Obtiene el horario semanal completo ordenado (Lunes a Domingo).
     */
    public List<StoreSchedule> getAllSchedules() {
        List<StoreSchedule> schedules = repository.findAll();
        // Ordenamos en memoria para asegurar Lunes primero
        schedules.sort(Comparator.comparing(StoreSchedule::getDayOfWeek));
        return schedules;
    }

    /**
     * Actualiza el horario de un día específico.
     */
    @Transactional
    public StoreSchedule updateSchedule(Long id, StoreSchedule newData) {
        StoreSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + id));

        schedule.setOpen(newData.isOpen());
        schedule.setOpeningTime(newData.getOpeningTime());
        schedule.setClosingTime(newData.getClosingTime());

        return repository.save(schedule);
    }


}