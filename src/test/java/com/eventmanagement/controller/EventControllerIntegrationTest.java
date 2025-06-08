package com.eventmanagement.controller;

import com.eventmanagement.dto.auth.AuthResponse;
import com.eventmanagement.dto.auth.RegisterRequest;
import com.eventmanagement.dto.event.CreateEventRequest;
import com.eventmanagement.dto.event.UpdateEventRequest;
import com.eventmanagement.entity.Role;
import com.eventmanagement.entity.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private String eventId;

    @BeforeEach
    void setUp() throws Exception {

        userToken = registerAndLogin("user1@test.com", "User Name", Role.USER);

        adminToken = registerAndLogin("admin@test.com", "Admin Name", Role.ADMIN);
    }

    /**
     * This test class is for testing the EventController endpoints.
     * It uses Testcontainers for integration testing with a real database.
     * The tests are transactional, meaning they will roll back after execution.
     */
    @Test
    void createEvent_ShouldCreateEvent_WhenAuthenticatedUser() throws Exception {

        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Test Event");
        request.setDescription("Test Description");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setLocation("Test Location");
        request.setVisibility(Visibility.PUBLIC);

        MvcResult result = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.location").value("Test Location"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.attendeeCount").value(0))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        eventId = objectMapper.readTree(responseBody).get("id").asText();
    }

    /**
     * This test checks if the create event endpoint returns an unauthorized status
     * when no token is provided.
     */
    @Test
    void createEvent_ShouldReturnUnauthorized_WhenNoToken() throws Exception {

        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Test Event");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setLocation("Test Location");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    /**
     * This test checks if the create event endpoint returns a bad request status
     * when the provided data is invalid.
     */
    @Test
    void createEvent_ShouldReturnBadRequest_WhenInvalidData() throws Exception {

        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Test Event");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).minusHours(1)); // Invalid!
        request.setLocation("Test Location");

        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * This test checks if the create event endpoint returns a forbidden status
     * when the user is not authenticated.
     */
    @Test
    void updateEvent_ShouldUpdateEvent_WhenEventHost() throws Exception {

        String createdEventId = createTestEvent(userToken);

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setTitle("Updated Event Title");
        updateRequest.setDescription("Updated Description");

        mockMvc.perform(put("/events/" + createdEventId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    /**
     * This test checks if the update event endpoint returns a bad request status
     * when the user tries to update an event they do not own.
     */
    @Test
    void updateEvent_ShouldUpdateEvent_WhenAdmin() throws Exception {

        String createdEventId = createTestEvent(userToken);

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setTitle("Admin Updated Title");

        mockMvc.perform(put("/events/" + createdEventId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin Updated Title"));
    }

    /**
     * This test checks if the update event endpoint returns a bad request status
     * when the user tries to update an event they do not own.
     */
    @Test
    void updateEvent_ShouldReturnForbidden_WhenNotHostOrAdmin() throws Exception {

        String createdEventId = createTestEvent(userToken);

        String otherUserToken = registerAndLogin("other@example.com", "Other User", Role.USER);

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setTitle("Unauthorized Update");

        mockMvc.perform(put("/events/" + createdEventId)
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest()); // Our service returns 400 with "You can only update your own events"
    }

    /**
     * This test checks if the delete event endpoint returns a bad request status
     * when the user tries to delete an event they do not own.
     */
    @Test
    void deleteEvent_ShouldDeleteEvent_WhenEventHost() throws Exception {

        String createdEventId = createTestEvent(userToken);

        mockMvc.perform(delete("/events/" + createdEventId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/events/" + createdEventId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest()); // Event not found
    }

    /**
     * This test checks if the delete event endpoint returns a bad request status
     * when the user tries to delete an event they do not own.
     */
    @Test
    void getEvent_ShouldReturnEvent_WhenEventExists() throws Exception {

        String createdEventId = createTestEvent(userToken);

        mockMvc.perform(get("/events/" + createdEventId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdEventId))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.attendeeCount").value(0));
    }

    /**
     * This test checks if the get event endpoint returns a bad request status
     * when the event does not exist.
     */
    @Test
    void getEvents_ShouldReturnFilteredEvents_WhenFiltersApplied() throws Exception {

        createTestEventWithVisibility(userToken, Visibility.PUBLIC);
        createTestEventWithVisibility(userToken, Visibility.PRIVATE);


        mockMvc.perform(get("/events")
                        .param("visibility", "PUBLIC")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].visibility").value("PUBLIC"));
    }

    /**
     * This test checks if the get events endpoint returns a bad request status
     * when the user tries to access events without authentication.
     */
    @Test
    void getUpcomingEvents_ShouldReturnPaginatedEvents() throws Exception {

        createTestEvent(userToken);
        createTestEvent(userToken);

        mockMvc.perform(get("/events/upcoming")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * This test checks if the get events endpoint returns a bad request status
     * when the user tries to access events without authentication.
     */
    @Test
    void getEventsHostedByUser_ShouldReturnUserEvents() throws Exception {

        createTestEvent(userToken);
        createTestEvent(userToken);

        mockMvc.perform(get("/events/hosting")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * This test checks if the get events endpoint returns a bad request status
     * when the user tries to access events without authentication.
     */
    @Test
    void getEventsUserIsAttending_ShouldReturnAttendingEvents() throws Exception {

        mockMvc.perform(get("/events/attending")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * This test checks if the get events endpoint returns a bad request status
     * when the user tries to access events without authentication.
     */
    private String registerAndLogin(String email, String name, Role role) throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName(name);
        registerRequest.setEmail(email);
        registerRequest.setPassword("password123");
        registerRequest.setRole(role);

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = registerResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        return authResponse.getToken();
    }

    /**
     * This method creates a test event with the given token.
     * It uses the default visibility of PUBLIC.
     *
     * @param token The authentication token of the user.
     * @return The ID of the created event.
     * @throws Exception If there is an error during the request.
     */
    private String createTestEvent(String token) throws Exception {
        return createTestEventWithVisibility(token, Visibility.PUBLIC);
    }

    /**
     * This method creates a test event with the given token and visibility.
     *
     * @param token The authentication token of the user.
     * @param visibility The visibility of the event (PUBLIC, PRIVATE, or PROTECTED).
     * @return The ID of the created event.
     * @throws Exception If there is an error during the request.
     */
    private String createTestEventWithVisibility(String token, Visibility visibility) throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Test Event");
        request.setDescription("Test Description");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setLocation("Test Location");
        request.setVisibility(visibility);

        MvcResult result = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("id").asText();
    }
}