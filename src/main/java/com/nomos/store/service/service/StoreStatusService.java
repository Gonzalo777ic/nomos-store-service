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




    /**
     * Busca la próxima fecha/hora de apertura iterando los próximos 7 días.
     */
    private LocalDateTime findNextOpening(LocalDateTime fromDateTime) {
        LocalDate dateIterator = fromDateTime.toLocalDate();

        for (int i = 0; i < 8; i++) {
            EffectiveSchedule schedule = getEffectiveSchedule(dateIterator);

            if (!schedule.isClosed()) {
                LocalDateTime potentialOpen = LocalDateTime.of(dateIterator, schedule.getOpeningTime());

                if (potentialOpen.isAfter(fromDateTime)) {
                    return potentialOpen;
                }
            }
            dateIterator = dateIterator.plusDays(1);
        }
        return null;
    }

    /**
     * Combina Tabla Base + Tabla Excepciones para obtener la "Verdad" de un día específico.
     * PRIORIDAD: Excepción > Horario Base
     */
    private EffectiveSchedule getEffectiveSchedule(LocalDate date) {
        Optional<StoreScheduleException> exception = exceptionRepository.findByDate(date);
        if (exception.isPresent()) {
            StoreScheduleException ex = exception.get();
            return new EffectiveSchedule(
                    ex.isClosed(),
                    ex.getOpeningTime(),
                    ex.getClosingTime(),
                    ex.getReason() // "Feriado", etc.
            );
        }

        Optional<StoreSchedule> base = scheduleRepository.findByDayOfWeek(date.getDayOfWeek());
        if (base.isPresent()) {
            StoreSchedule sch = base.get();
            return new EffectiveSchedule(
                    !sch.isOpen(), // Si isOpen es false, isClosed es true
                    sch.getOpeningTime(),
                    sch.getClosingTime(),
                    null
            );
        }

        return new EffectiveSchedule(true, null, null, "Sin configuración");
    }

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