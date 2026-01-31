package com.nomos.store.service.model;

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


}