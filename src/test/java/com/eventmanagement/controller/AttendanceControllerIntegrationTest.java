package com.eventmanagement.controller;

import com.eventmanagement.dto.attendance.CreateAttendanceRequest;
import com.eventmanagement.dto.attendance.UpdateAttendanceRequest;
import com.eventmanagement.dto.auth.AuthResponse;
import com.eventmanagement.dto.auth.RegisterRequest;
import com.eventmanagement.dto.event.CreateEventRequest;
import com.eventmanagement.entity.AttendanceStatus;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "spring.cache.type=none"
})
class AttendanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String eventId;

    @BeforeEach
    void setUp() throws Exception {
        userToken = registerUser("user1@test.com");
        eventId = createEvent(userToken);
    }

    // Test 1: POST /attendance - Should create an attendance when valid request
    @Test
    void respondToAnEvent_ShouldCreateAttendance_WhenValidRequest() throws Exception {
        CreateAttendanceRequest request = new CreateAttendanceRequest();
        request.setEventId(UUID.fromString(eventId));
        request.setStatus(AttendanceStatus.GOING);

        mockMvc.perform(post("/attendance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.status").value("GOING"));
    }

   // Test 2: POST /attendance - Should return bad request when already responded
    @Test
    void respondToAnEvent_ShouldReturnBadRequest_WhenAlreadyResponded() throws Exception {

        CreateAttendanceRequest request = new CreateAttendanceRequest();
        request.setEventId(UUID.fromString(eventId));
        request.setStatus(AttendanceStatus.GOING);

        mockMvc.perform(post("/attendance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/attendance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test 3: PUT /attendance/events/{eventId} - Should update attendance
    @Test
    void updateAttendance_ShouldUpdateStatus_WhenAlreadyResponded() throws Exception {

        CreateAttendanceRequest createRequest = new CreateAttendanceRequest();
        createRequest.setEventId(UUID.fromString(eventId));
        createRequest.setStatus(AttendanceStatus.MAYBE);

        mockMvc.perform(post("/attendance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        UpdateAttendanceRequest updateRequest = new UpdateAttendanceRequest();
        updateRequest.setStatus(AttendanceStatus.GOING);

        mockMvc.perform(put("/attendance/events/" + eventId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GOING"));
    }

    // Test 4: PUT /attendance/events/{eventId} - Should return 400 when not responded
    @Test
    void updateAttendance_ShouldReturnBadRequest_WhenNotResponded() throws Exception {
        UpdateAttendanceRequest updateRequest = new UpdateAttendanceRequest();
        updateRequest.setStatus(AttendanceStatus.GOING);

        mockMvc.perform(put("/attendance/events/" + eventId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    // Test 5: GET /attendance/events/{eventId}/my-status - Should return status
    @Test
    void getMyAttendanceStatus_ShouldReturnStatus_WhenResponded() throws Exception {

        CreateAttendanceRequest request = new CreateAttendanceRequest();
        request.setEventId(UUID.fromString(eventId));
        request.setStatus(AttendanceStatus.GOING);

        mockMvc.perform(post("/attendance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/attendance/events/" + eventId + "/my-status")
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GOING"));
    }

    // Test 6: GET /attendance/events/{eventId}/my-status - Should return 400 when not responded
    @Test
    void getMyAttendanceStatus_ShouldReturnBadRequest_WhenNotResponded() throws Exception {
        mockMvc.perform(get("/attendance/events/" + eventId + "/my-status")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    private String registerUser(String email) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail(email);
        request.setPassword("password123");
        request.setRole(Role.USER);

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        return response.getToken();
    }

    private String createEvent(String token) throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Test Event");
        request.setDescription("Test Description");
        request.setStartTime(LocalDateTime.now().plusDays(7));
        request.setEndTime(LocalDateTime.now().plusDays(7).plusHours(2));
        request.setLocation("Test Location");
        request.setVisibility(Visibility.PUBLIC);

        MvcResult result = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}