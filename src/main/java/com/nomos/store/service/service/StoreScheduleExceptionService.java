package com.nomos.store.service.service;

import com.nomos.store.service.model.StoreScheduleException;
import com.nomos.store.service.repository.StoreScheduleExceptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreScheduleExceptionService {

    private final StoreScheduleExceptionRepository repository;

    /**
     * Obtiene todas las excepciones futuras y presentes (desde HOY en adelante).
     * No suele ser útil traer feriados de hace 3 años.
     */
    public List<StoreScheduleException> getUpcomingExceptions() {
        return repository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now());
    }


}