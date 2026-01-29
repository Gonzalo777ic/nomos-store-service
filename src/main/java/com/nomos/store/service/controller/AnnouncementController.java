package com.nomos.store.service.controller;

import com.nomos.store.service.model.Announcement;
import com.nomos.store.service.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService service;

    /**
     * Obtener todos los anuncios (Para gestión administrativa)
     * GET /api/store/announcements
     */
    @GetMapping
    public ResponseEntity<List<Announcement>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Obtener solo anuncios activos y vigentes (Para mostrar al usuario final)
     * GET /api/store/announcements/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Announcement>> getActive() {
        return ResponseEntity.ok(service.getActiveAnnouncements());
    }

    /**
     * Obtener un anuncio por ID
     * GET /api/store/announcements/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Crear un nuevo anuncio
     * POST /api/store/announcements
     */
    @PostMapping
    public ResponseEntity<Announcement> create(@RequestBody Announcement announcement) {
        return ResponseEntity.ok(service.create(announcement));
    }

    /**
     * Actualizar un anuncio existente
     * PUT /api/store/announcements/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Announcement> update(
            @PathVariable Long id,
            @RequestBody Announcement announcement) {
        return ResponseEntity.ok(service.update(id, announcement));
    }

    /**
     * Alternar estado Activo/Inactivo (Switch rápido)
     * PATCH /api/store/announcements/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Announcement> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleActive(id));
    }


}