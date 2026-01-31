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
    /**
     * Crea una nueva excepción (Feriado u Horario Especial).
     */
    @Transactional
    public StoreScheduleException create(StoreScheduleException exception) {
        if (exception.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden crear excepciones para fechas pasadas.");
        }

        if (repository.existsByDate(exception.getDate())) {
            throw new IllegalArgumentException("Ya existe una configuración especial para la fecha " + exception.getDate());
        }

        return repository.save(exception);
    }
    /**
     * Actualizar una excepción existente.
     */
    @Transactional
    public StoreScheduleException update(Long id, StoreScheduleException newData) {
        StoreScheduleException existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Excepción no encontrada con ID: " + id));

        existing.setClosed(newData.isClosed());
        existing.setOpeningTime(newData.getOpeningTime());
        existing.setClosingTime(newData.getClosingTime());
        existing.setReason(newData.getReason());

        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Excepción no encontrada");
        }
        repository.deleteById(id);
    }


}