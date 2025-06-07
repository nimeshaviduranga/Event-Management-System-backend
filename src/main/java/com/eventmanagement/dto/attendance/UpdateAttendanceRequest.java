package com.eventmanagement.dto.attendance;

import com.eventmanagement.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateAttendanceRequest {

    @NotNull
    private AttendanceStatus status;
}