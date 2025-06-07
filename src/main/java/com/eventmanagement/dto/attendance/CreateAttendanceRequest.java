package com.eventmanagement.dto.attendance;

import com.eventmanagement.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateAttendanceRequest {

    @NotNull
    private UUID eventId;

    @NotNull
    private AttendanceStatus status;
}