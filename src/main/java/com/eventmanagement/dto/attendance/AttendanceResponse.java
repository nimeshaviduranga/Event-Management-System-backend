package com.eventmanagement.dto.attendance;

import com.eventmanagement.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private String id;
    private String eventId;
    private String eventTitle;
    private String userId;
    private String userName;
    private AttendanceStatus status;
    private LocalDateTime respondedAt;
}