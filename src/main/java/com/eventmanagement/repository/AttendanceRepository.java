package com.eventmanagement.repository;

import com.eventmanagement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    //Find attendance by event and user IDs
    Optional<Attendance> findByEventIdAndUserId(UUID eventId, UUID userId);

    //Check if attendance exists
    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    //List event user attending
    @Query("SELECT a.eventId FROM Attendance a WHERE a.userId = :userId")
    List<UUID> findEventIdsByUserId(@Param("userId") UUID userId);

    //Count attendees for event
    long countByEventId(UUID eventId);
}