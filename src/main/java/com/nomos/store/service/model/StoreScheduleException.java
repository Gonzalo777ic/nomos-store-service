package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "store_schedule_exceptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = "exception_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreScheduleException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exception_date", nullable = false, unique = true)
    private LocalDate date;

    @JsonProperty("isClosed")
    @Column(name = "is_closed", nullable = false)
    private boolean isClosed;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "opening_time")
    private LocalTime openingTime;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(nullable = false)
    private String reason;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (!isClosed) {
            if (openingTime == null || closingTime == null) {
                throw new IllegalArgumentException("Si el local abre en fecha especial, debe definir hora de inicio y fin.");
            }
            if (closingTime.isBefore(openingTime)) {
                throw new IllegalArgumentException("La hora de cierre no puede ser anterior a la de apertura.");
            }
        }
    }

}