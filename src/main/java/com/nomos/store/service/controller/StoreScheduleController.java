package com.nomos.store.service.controller;

import com.nomos.store.service.model.StoreSchedule;
import com.nomos.store.service.service.StoreScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/schedule")
@RequiredArgsConstructor
public class StoreScheduleController {

    private final StoreScheduleService service;

    @GetMapping
    public ResponseEntity<List<StoreSchedule>> getWeeklySchedule() {
        return ResponseEntity.ok(service.getAllSchedules());
    }


    @PutMapping("/{id}")
    public ResponseEntity<StoreSchedule> updateDaySchedule(
            @PathVariable Long id,
            @RequestBody StoreSchedule schedule) {

        System.out.println("RECIBIENDO UPDATE PARA ID: " + id);
        System.out.println("Hora Apertura llega como: " + schedule.getOpeningTime());
        System.out.println("Hora Cierre llega como: " + schedule.getClosingTime());

        return ResponseEntity.ok(service.updateSchedule(id, schedule));
    }
}