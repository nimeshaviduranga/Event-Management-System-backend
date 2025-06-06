package com.eventmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "user_id"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.MAYBE;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    //Constructor
    public Attendance(UUID eventId, UUID userId, AttendanceStatus status) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
        this.respondedAt = LocalDateTime.now();
    }

    //utility methods to check whether the Attendance status
    public void updateStatus(AttendanceStatus newStatus) {
        this.status = newStatus;
        this.respondedAt = LocalDateTime.now();
    }

    public boolean isGoing() {
        return AttendanceStatus.GOING.equals(this.status);
    }

    public boolean isMaybe() {
        return AttendanceStatus.MAYBE.equals(this.status);
    }

    public boolean isDeclined() {
        return AttendanceStatus.DECLINED.equals(this.status);
    }
}