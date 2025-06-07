package com.eventmanagement.controller;

import com.eventmanagement.dto.attendance.AttendanceResponse;
import com.eventmanagement.dto.attendance.CreateAttendanceRequest;
import com.eventmanagement.dto.attendance.UpdateAttendanceRequest;
import com.eventmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Respond to an event
     * POST /api/v1/attendance
     */
    @PostMapping
    public ResponseEntity<AttendanceResponse> respondToAnEvent(@Valid @RequestBody CreateAttendanceRequest request) {
        AttendanceResponse response = attendanceService.respondToAnEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update attendance for an event
     * PUT /api/v1/attendance/events/{eventId}
     */
    @PutMapping("/events/{eventId}")
    public ResponseEntity<AttendanceResponse> updateAttendance(@PathVariable UUID eventId,
                                                               @Valid @RequestBody UpdateAttendanceRequest request) {
        AttendanceResponse response = attendanceService.updateTheAttendance(eventId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get attendance status for an event
     * GET /api/v1/attendance/events/{eventId}/status
     */
    @GetMapping("/events/{eventId}/my-status")
    public ResponseEntity<AttendanceResponse> getMyAttendanceStatus(@PathVariable UUID eventId) {
        AttendanceResponse response = attendanceService.getMyAttendanceStatus(eventId);
        return ResponseEntity.ok(response);
    }
}