package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "store_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = "day_of_week")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, unique = true)
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "opening_time")
    private LocalTime openingTime;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "closing_time")
    private LocalTime closingTime;

    @JsonProperty("isOpen")
    @Column(name = "is_open", nullable = false)
    private boolean isOpen;

    @PrePersist
    @PreUpdate
    private void validateHours() {
        if (isOpen && openingTime != null && closingTime != null) {
            if (closingTime.isBefore(openingTime)) {
                throw new IllegalArgumentException("La hora de cierre no puede ser anterior a la hora de apertura.");
            }
        }
    }
}