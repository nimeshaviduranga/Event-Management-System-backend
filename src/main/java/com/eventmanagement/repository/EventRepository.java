package com.eventmanagement.repository;

import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    //Find event by host ID
    Page<Event> findByHostId(UUID hostId, Pageable pageable);

    //FInd upcoming events(pagination included)
    @Query("SELECT e FROM Event e WHERE e.startTime > :now ORDER BY e.startTime ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    //Find events by different criteria(pagination included)
    @Query("SELECT e FROM Event e WHERE " +
            "(:visibility IS NULL OR e.visibility = :visibility) AND " +
            "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:startDate IS NULL OR e.startTime >= :startDate) AND " +
            "(:endDate IS NULL OR e.startTime <= :endDate)")
    Page<Event> findEventsByCriteria(@Param("visibility") Visibility visibility,
                                     @Param("location") String location,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);
}