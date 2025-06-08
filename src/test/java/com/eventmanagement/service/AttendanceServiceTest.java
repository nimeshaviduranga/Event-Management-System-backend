package com.eventmanagement.service;

import com.eventmanagement.dto.attendance.CreateAttendanceRequest;
import com.eventmanagement.dto.attendance.AttendanceResponse;
import com.eventmanagement.dto.attendance.UpdateAttendanceRequest;
import com.eventmanagement.entity.Attendance;
import com.eventmanagement.entity.AttendanceStatus;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.mapper.AttendanceMapper;
import com.eventmanagement.repository.AttendanceRepository;
import com.eventmanagement.repository.EventRepository;
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AttendanceMapper attendanceMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private AttendanceService attendanceService;

    private CreateAttendanceRequest createRequest;
    private UpdateAttendanceRequest updateRequest;
    private Event event;
    private User user;
    private Attendance attendance;
    private UUID userId;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        createRequest = new CreateAttendanceRequest();
        createRequest.setEventId(eventId);
        createRequest.setStatus(AttendanceStatus.GOING);

        updateRequest = new UpdateAttendanceRequest();
        updateRequest.setStatus(AttendanceStatus.MAYBE);

        event = new Event();
        event.setId(eventId);
        event.setTitle("Test Event");

        user = new User();
        user.setId(userId);
        user.setName("Test User");

        attendance = new Attendance();
        attendance.setId(UUID.randomUUID());
        attendance.setEventId(eventId);
        attendance.setUserId(userId);
        attendance.setStatus(AttendanceStatus.GOING);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(userId);

        lenient().when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    /**
     * Test for responding to an event
     */
    @Test
    void respondToAnEvent_ShouldCreateAttendance_WhenValidRequest() {

        when(attendanceRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        AttendanceResponse expectedResponse = new AttendanceResponse();
        expectedResponse.setEventId(eventId.toString());
        expectedResponse.setStatus(AttendanceStatus.GOING);
        when(attendanceMapper.toResponse(any(), anyString(), anyString())).thenReturn(expectedResponse);

        AttendanceResponse response = attendanceService.respondToAnEvent(createRequest);

        assertThat(response).isNotNull();
        verify(eventRepository).findById(eventId); // Verify event validation happens first
        verify(attendanceRepository).existsByEventIdAndUserId(eventId, userId);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    /**
     * Test for responding to an event with various scenarios
     */
    @Test
    void respondToAnEvent_ShouldThrowException_WhenEventNotFound() {

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.respondToAnEvent(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Event not found");

        verify(attendanceRepository, never()).existsByEventIdAndUserId(any(), any());
        verify(attendanceRepository, never()).save(any());
    }

    /**
     * Test for responding to an event when the user has already responded
     */
    @Test
    void respondToAnEvent_ShouldThrowException_WhenAlreadyResponded() {

        when(attendanceRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);

        assertThatThrownBy(() -> attendanceService.respondToAnEvent(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Already responded to the event");

        verify(eventRepository).findById(eventId);
        verify(attendanceRepository).existsByEventIdAndUserId(eventId, userId);
        verify(attendanceRepository, never()).save(any());
    }

    /**
     * Test for updating attendance status
     */
    @Test
    void updateTheAttendance_ShouldUpdateStatus_WhenAttendanceExists() {

        when(attendanceRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        AttendanceResponse expectedResponse = new AttendanceResponse();
        expectedResponse.setEventId(eventId.toString());
        expectedResponse.setStatus(AttendanceStatus.MAYBE);
        when(attendanceMapper.toResponse(any(), anyString(), anyString())).thenReturn(expectedResponse);

        AttendanceResponse response = attendanceService.updateTheAttendance(eventId, updateRequest);

        assertThat(response).isNotNull();
        verify(attendanceRepository).findByEventIdAndUserId(eventId, userId);
        verify(attendanceRepository).save(any(Attendance.class));
        verify(eventRepository).findById(eventId); // Verify mapping dependencies
        verify(userRepository).findById(userId);
    }

    /**
     * Test for updating attendance status when the attendance does not exist
     */
    @Test
    void updateTheAttendance_ShouldThrowException_WhenAttendanceNotFound() {

        when(attendanceRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.updateTheAttendance(eventId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not responded to the event yet");

        verify(attendanceRepository).findByEventIdAndUserId(eventId, userId);
        verify(attendanceRepository, never()).save(any());
    }

    /**
     * Test for getting my attendance status
     */
    @Test
    void getMyAttendanceStatus_ShouldReturnStatus_WhenAttendanceExists() {

        when(attendanceRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.of(attendance));

        AttendanceResponse expectedResponse = new AttendanceResponse();
        expectedResponse.setEventId(eventId.toString());
        expectedResponse.setUserId(userId.toString());
        expectedResponse.setStatus(AttendanceStatus.GOING);
        expectedResponse.setEventTitle("Test Event");
        expectedResponse.setUserName("Test User");
        when(attendanceMapper.toResponse(any(), anyString(), anyString())).thenReturn(expectedResponse);

        AttendanceResponse response = attendanceService.getMyAttendanceStatus(eventId);

        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(eventId.toString());
        assertThat(response.getStatus()).isEqualTo(AttendanceStatus.GOING);

        verify(attendanceRepository).findByEventIdAndUserId(eventId, userId);
        verify(eventRepository).findById(eventId); // Verify mapping dependencies
        verify(userRepository).findById(userId);
        verify(attendanceMapper).toResponse(attendance, "Test Event", "Test User");
    }

    /**
     * Test for getting my attendance status when the attendance does not exist
     */
    @Test
    void getMyAttendanceStatus_ShouldThrowException_WhenAttendanceNotFound() {

        when(attendanceRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.getMyAttendanceStatus(eventId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not responded to the event yet"); // Fixed error message

        verify(attendanceRepository).findByEventIdAndUserId(eventId, userId);
        verify(eventRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
    }
}