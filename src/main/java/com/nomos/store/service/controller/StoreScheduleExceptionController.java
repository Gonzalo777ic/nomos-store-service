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


}