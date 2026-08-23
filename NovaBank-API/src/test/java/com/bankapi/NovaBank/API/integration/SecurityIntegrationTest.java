package com.bankapi.NovaBank.API.integration;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
import com.bankapi.NovaBank.API.dto.request.LoginRequest;
import com.bankapi.NovaBank.API.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private String token;
    private String email;
    private final String password = "password123";

    @BeforeEach
    void setup() throws Exception {

        // Use a unique user for every test to prevent
        // database conflicts between test methods/classes.
        email = "john-" + UUID.randomUUID() + "@gmail.com";

        RegisterRequest register =
                new RegisterRequest(
                        "John Doe",
                        email,
                        password
                );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequest login =
                new LoginRequest(
                        email,
                        password
                );

        String json =
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(login)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        token = mapper.readTree(json)
                .get("token")
                .asText();
    }

    @Test
    void shouldAllowRegistrationWithoutAuthentication() throws Exception {

        String newEmail = "jane-" + UUID.randomUUID() + "@gmail.com";

        RegisterRequest request =
                new RegisterRequest(
                        "Jane Doe",
                        newEmail,
                        password
                );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"));
    }

    @Test
    void shouldAllowLoginWithoutAuthentication() throws Exception {

        LoginRequest login =
                new LoginRequest(
                        email,
                        password
                );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {

        mockMvc.perform(post("/api/accounts/create"))
                .andExpect(status().isUnauthorized());    }

    @Test
    void shouldRejectInvalidToken() throws Exception {

        mockMvc.perform(post("/api/accounts/create")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());    }

    @Test
    void shouldAllowValidToken() throws Exception {

        mockMvc.perform(post("/api/accounts/create")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectMalformedBearerToken() throws Exception {

        mockMvc.perform(post("/api/accounts/create")
                        .header("Authorization", "Bearer"))
                .andExpect(status().isUnauthorized());    }

    @Test
    void shouldRejectWrongAuthorizationHeader() throws Exception {

        mockMvc.perform(post("/api/accounts/create")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isUnauthorized());    }
}