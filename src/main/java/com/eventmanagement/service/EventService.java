package com.eventmanagement.service;

import com.eventmanagement.dto.event.CreateEventRequest;
import com.eventmanagement.dto.event.EventResponse;
import com.eventmanagement.dto.event.UpdateEventRequest;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.entity.Visibility;
import com.eventmanagement.repository.AttendanceRepository;
import com.eventmanagement.repository.EventRepository;
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Create new event
     */
    @Transactional
    public EventResponse createAnEvent(CreateEventRequest request) {
        UUID currentUserId = getCurrentUserId();

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("ENd time should be after the start time");
        }

        Event event = new Event(
                request.getTitle(),
                request.getDescription(),
                currentUserId,
                request.getStartTime(),
                request.getEndTime(),
                request.getLocation(),
                request.getVisibility()
        );

        Event savedEvent = eventRepository.save(event);
        log.info("Event created: {} by user: {}", savedEvent.getId(), currentUserId);

        return mapToEventResponse(savedEvent);
    }

    /**
     * Update Event
     */
    @Transactional
    public EventResponse updateAnEvent(UUID eventId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event has not been found"));

        UUID currentUserId = getCurrentUserId();

        if (!event.getHostId().equals(currentUserId) && isCurrentUserAdmin()) {
            throw new RuntimeException("Access denied");
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getVisibility() != null) {
            event.setVisibility(request.getVisibility());
        }

        if (event.getEndTime().isBefore(event.getStartTime())) {
            throw new RuntimeException("ENd time should be after the start time");
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated: {} by user: {}", eventId, currentUserId);

        return mapToEventResponse(updatedEvent);
    }

    /**
     * Delete Event
     */
    @Transactional
    public void deleteAnEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event has not been found"));

        UUID currentUserId = getCurrentUserId();

        // Check if user is host or admin
        if (!event.getHostId().equals(currentUserId) && isCurrentUserAdmin()) {
            throw new RuntimeException("Access denied");
        }

        eventRepository.delete(event);
        log.info("Event deleted: {} by user: {}", eventId, currentUserId);
    }

    /**
     * Get Event by id
     */
    public EventResponse getAnEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event has not been found"));

        return mapToEventResponse(event);
    }

    /**
     * List events
     */
    public Page<EventResponse> getEventsWithFilter(Visibility visibility, String location,
                                                    LocalDateTime startDate, LocalDateTime endDate,
                                                    Pageable pageable) {
        Page<Event> events = eventRepository.findEventsByCriteria(
                visibility, location, startDate, endDate, pageable);

        return events.map(this::mapToEventResponse);
    }

    /**
     * List upcoming events
     */
    public Page<EventResponse> getUpcomingEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
        return events.map(this::mapToEventResponse);
    }

    /**
     * List hosted events by user
     */
    public Page<EventResponse> getEventsHostedByUser(Pageable pageable) {
        UUID currentUserId = getCurrentUserId();
        Page<Event> events = eventRepository.findByHostId(currentUserId, pageable);
        return events.map(this::mapToEventResponse);
    }

    /**
     * List events user is attending
     */
    public List<EventResponse> getEventsUserIsAttending() {
        UUID currentUserId = getCurrentUserId();
        List<UUID> eventIds = attendanceRepository.findEventIdsByUserId(currentUserId);

        List<Event> events = eventIds.stream()
                .map(eventId -> eventRepository.findById(eventId).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return events.stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map event entity to event response DTO
     */
    private EventResponse mapToEventResponse(Event event) {
        long attendeeCount = attendanceRepository.countByEventId(event.getId());

        String hostName = userRepository.findById(event.getHostId())
                .map(User::getName)
                .orElse("Unknown Host");

        return new EventResponse(
                event.getId().toString(),
                event.getTitle(),
                event.getDescription(),
                event.getHostId().toString(),
                hostName,
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getVisibility(),
                attendeeCount,
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    /**
     * Get authenticated user's ID
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Check if the current user is an admin
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}