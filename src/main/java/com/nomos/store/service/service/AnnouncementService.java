package com.nomos.store.service.service;

import com.nomos.store.service.model.Announcement;
import com.nomos.store.service.repository.AnnouncementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository repository;

    /**
     * Obtiene solo los anuncios que deben mostrarse actualmente al usuario.
     * Filtra por estado activo y rango de fechas vigente.
     */
    public List<Announcement> getActiveAnnouncements() {
        return repository.findActiveAnnouncements(LocalDateTime.now());
    }

    /**
     * Obtiene todos los anuncios (para el panel de administración).
     * Podrías ordenarlos por fecha de creación descendente si quisieras.
     */
    public List<Announcement> getAll() {
        return repository.findAll();
    }

    /**
     * Busca un anuncio por ID o lanza excepción si no existe.
     */
    public Announcement getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anuncio no encontrado con ID: " + id));
    }
    /**
     * Crea un nuevo anuncio validando la coherencia de las fechas.
     */
    @Transactional
    public Announcement create(Announcement announcement) {
        validateDates(announcement.getStartDate(), announcement.getEndDate());
        return repository.save(announcement);
    }

    /**
     * Actualiza un anuncio existente.
     */
    @Transactional
    public Announcement update(Long id, Announcement updatedData) {
        Announcement existing = getById(id);

        validateDates(updatedData.getStartDate(), updatedData.getEndDate());

        existing.setTitle(updatedData.getTitle());
        existing.setContent(updatedData.getContent());
        existing.setType(updatedData.getType());
        existing.setStartDate(updatedData.getStartDate());
        existing.setEndDate(updatedData.getEndDate());
        existing.setTargetAudience(updatedData.getTargetAudience());

        existing.setActive(updatedData.isActive());

        return repository.save(existing);
    }



    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("La fecha de inicio (startDate) no puede ser posterior a la fecha de fin (endDate).");
            }
        }
    }

}