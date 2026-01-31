package com.nomos.store.service.service;

import com.nomos.store.service.model.StoreSchedule;
import com.nomos.store.service.model.StoreScheduleException;
import com.nomos.store.service.model.dto.StoreStatusDTO;
import com.nomos.store.service.repository.StoreScheduleExceptionRepository;
import com.nomos.store.service.repository.StoreScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreStatusService {

    private final StoreScheduleRepository scheduleRepository;
    private final StoreScheduleExceptionRepository exceptionRepository;



    @lombok.Value
    private static class EffectiveSchedule {
        boolean isClosed;
        LocalTime openingTime;
        LocalTime closingTime;
        String reason;
    }

    private String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("h:mm a"));
    }

    private String mapDayName(String englishDay) {
        switch (englishDay) {
            case "MONDAY": return "Lunes";
            case "TUESDAY": return "Martes";
            case "WEDNESDAY": return "Miércoles";
            case "THURSDAY": return "Jueves";
            case "FRIDAY": return "Viernes";
            case "SATURDAY": return "Sábado";
            case "SUNDAY": return "Domingo";
            default: return "";
        }
    }
}