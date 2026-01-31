package com.nomos.store.service.controller;

import com.nomos.store.service.model.StoreScheduleException;
import com.nomos.store.service.service.StoreScheduleExceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/schedule/exceptions")
@RequiredArgsConstructor
public class StoreScheduleExceptionController {

    private final StoreScheduleExceptionService service;

    @GetMapping
    public ResponseEntity<List<StoreScheduleException>> getUpcoming() {
        return ResponseEntity.ok(service.getUpcomingExceptions());
    }

    @PostMapping
    public ResponseEntity<StoreScheduleException> create(@RequestBody StoreScheduleException exception) {
        return ResponseEntity.ok(service.create(exception));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreScheduleException> update(
            @PathVariable Long id,
            @RequestBody StoreScheduleException exception) {
        return ResponseEntity.ok(service.update(id, exception));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}