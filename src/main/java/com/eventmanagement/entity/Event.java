package com.eventmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "host_id", nullable = false)
    private UUID hostId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

   //Constructor
    public Event(String title, String description, UUID hostId,
                 LocalDateTime startTime, LocalDateTime endTime,
                 String location, Visibility visibility) {
        this.title = title;
        this.description = description;
        this.hostId = hostId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.visibility = visibility;
    }

    // Utility methods
    public boolean isUpcoming() {
        return startTime.isAfter(LocalDateTime.now());
    }

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && now.isBefore(endTime);
    }

    public boolean isPast() {
        return endTime.isBefore(LocalDateTime.now());
    }

    public boolean isPublic() {
        return Visibility.PUBLIC.equals(this.visibility);
    }
}