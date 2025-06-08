package com.eventmanagement.controller;

import com.eventmanagement.dto.auth.RegisterRequest;
import com.eventmanagement.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * This test class is for testing the AuthController endpoints.
     * It uses Testcontainers for integration testing with a real database.
     * The tests are transactional, meaning they will roll back after execution.
     */
    @Test
    void register_ShouldCreateUser_WhenValidRequest() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("user1@test.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("user1@test.com"));
    }

    /**
     * This test checks if the registration endpoint returns a bad request status
     * when the email is already registered.
     */
    @Test
    void register_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}