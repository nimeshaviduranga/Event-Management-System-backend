package com.eventmanagement.controller;

import com.eventmanagement.dto.event.CreateEventRequest;
import com.eventmanagement.dto.event.EventResponse;
import com.eventmanagement.dto.event.UpdateEventRequest;
import com.eventmanagement.entity.Visibility;
import com.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * POST /api/v1/events
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createAnEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/events/{eventId}
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID eventId,
                                                     @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.updateAnEvent(eventId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/events/{eventId}
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteAnEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/events/{eventId}
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID eventId) {
        EventResponse response = eventService.getAnEvent(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/events/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventResponse>> getUpcomingEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EventResponse> response = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/events?visibility=PUBLIC&location=Colombo&startDate=2025-07-06T00:00:00
     */
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EventResponse> response = eventService.getEventsWithFilter(
                visibility, location, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/events/hosting
     */
    @GetMapping("/hosting")
    public ResponseEntity<Page<EventResponse>> getEventsHostedByUser(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EventResponse> response = eventService.getEventsHostedByUser(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/events/attending
     */
    @GetMapping("/attending")
    public ResponseEntity<List<EventResponse>> getEventsUserIsAttending() {
        List<EventResponse> response = eventService.getEventsUserIsAttending();
        return ResponseEntity.ok(response);
    }
}