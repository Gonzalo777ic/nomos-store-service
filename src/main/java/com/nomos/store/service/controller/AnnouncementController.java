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


}