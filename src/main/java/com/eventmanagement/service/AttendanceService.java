package com.eventmanagement.service;

import com.eventmanagement.dto.attendance.AttendanceResponse;
import com.eventmanagement.dto.attendance.CreateAttendanceRequest;
import com.eventmanagement.dto.attendance.UpdateAttendanceRequest;
import com.eventmanagement.entity.Attendance;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.mapper.AttendanceMapper;
import com.eventmanagement.repository.AttendanceRepository;
import com.eventmanagement.repository.EventRepository;
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;

    /**
     * Respond to an event
     */
    @Transactional
    public AttendanceResponse respondToAnEvent(CreateAttendanceRequest request) {
        UUID currentUserId = getCurrentUserId();

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (attendanceRepository.existsByEventIdAndUserId(request.getEventId(), currentUserId)) {
            throw new RuntimeException("Already responded to the event");
        }

        Attendance attendance = new Attendance(
                request.getEventId(),
                currentUserId,
                request.getStatus()
        );

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return mapToAttendanceResponse(savedAttendance);
    }

    /**
     * Update attendance status for an event
     */
    @Transactional
    public AttendanceResponse updateTheAttendance(UUID eventId, UpdateAttendanceRequest request) {
        UUID currentUserId = getCurrentUserId();

        // Find existing attendance
        Attendance attendance = attendanceRepository.findByEventIdAndUserId(eventId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Not responded to the event yet"));

        // Update status
        attendance.updateStatus(request.getStatus());
        Attendance updatedAttendance = attendanceRepository.save(attendance);

        log.info("User {} updated attendance to {} for event {}", currentUserId, request.getStatus(), eventId);

        return mapToAttendanceResponse(updatedAttendance);
    }

    /**
     * Get my attendance status for a specific event
     */
    public AttendanceResponse getMyAttendanceStatus(UUID eventId) {
        UUID currentUserId = getCurrentUserId();

        Attendance attendance = attendanceRepository.findByEventIdAndUserId(eventId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Not responded to the event yet"));

        return mapToAttendanceResponse(attendance);
    }

    /**
     * Map Attendance entity to AttendanceResponse DTO
     */
    private AttendanceResponse mapToAttendanceResponse(Attendance attendance) {
        Event event = eventRepository.findById(attendance.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        User user = userRepository.findById(attendance.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return attendanceMapper.toResponse(attendance, event.getTitle(), user.getName()); // Using MapStruct
    }

    /**
     * Get the current user's ID from the security context
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}