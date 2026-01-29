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


}