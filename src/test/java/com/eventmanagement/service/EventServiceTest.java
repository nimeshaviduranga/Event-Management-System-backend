package com.eventmanagement.service;

import com.eventmanagement.dto.event.CreateEventRequest;
import com.eventmanagement.dto.event.EventResponse;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.entity.Visibility;
import com.eventmanagement.mapper.EventMapper;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private EventService eventService;

    private CreateEventRequest createEventRequest;
    private Event event;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        createEventRequest = new CreateEventRequest();
        createEventRequest.setTitle("Test Event");
        createEventRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createEventRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        createEventRequest.setLocation("Test Location");
        createEventRequest.setVisibility(Visibility.PUBLIC);

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Test Event");
        event.setHostId(userId);

        user = new User();
        user.setId(userId);
        user.setName("Test User");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(userId);
    }

    /**
     * Test for creating an event
     */
    @Test
    void createEvent_ShouldCreateEvent_WhenValidRequest() {
        when(eventMapper.toEntity(any())).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(attendanceRepository.countByEventId(any())).thenReturn(0L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventMapper.toResponse(any(), anyString(), anyLong())).thenReturn(new EventResponse());
S
        EventResponse response = eventService.createAnEvent(createEventRequest);

        assertThat(response).isNotNull();
        verify(eventRepository).save(any(Event.class));
    }

    /**
     * Test for creating an event with a title exceeding the character limit
     */
    @Test
    void createEvent_ShouldThrowException_WhenEndTimeBeforeStartTime() {
        createEventRequest.setEndTime(LocalDateTime.now().minusHours(1));

        assertThatThrownBy(() -> eventService.createAnEvent(createEventRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ENd time should be after the start time");
    }
}