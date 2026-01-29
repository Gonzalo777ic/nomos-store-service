package com.nomos.store.service.repository;

import com.nomos.store.service.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("SELECT a FROM Announcement a " +
            "WHERE a.isActive = true " +
            "AND (a.startDate IS NULL OR a.startDate <= :now) " +
            "AND (a.endDate IS NULL OR a.endDate >= :now) " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(LocalDateTime now);
}