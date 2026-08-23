package com.bankapi.NovaBank.API.controller;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
import com.bankapi.NovaBank.API.dto.request.LoginRequest;
import com.bankapi.NovaBank.API.dto.request.RegisterRequest;
import com.bankapi.NovaBank.API.dto.response.AuthResponse;
import com.bankapi.NovaBank.API.dto.response.RegisterResponse;
import com.bankapi.NovaBank.API.entity.Role;
import com.bankapi.NovaBank.API.exception.UserAlreadyExistsException;
import com.bankapi.NovaBank.API.exception.UserNotFoundException;
import com.bankapi.NovaBank.API.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "john@gmail.com",
                "password123"
        );

        RegisterResponse response =
                new RegisterResponse("User registered successfully");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn("User registered successfully");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {

        LoginRequest request =
                new LoginRequest(
                        "john@gmail.com",
                        "password123"
                );

        AuthResponse response =
                new AuthResponse(
                        "jwt-token",
                        "john@gmail.com",
                        Role.USER
                );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("jwt-token"))
                .andExpect(jsonPath("$.email")
                        .value("john@gmail.com"))
                .andExpect(jsonPath("$.role")
                        .value("USER"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void shouldReturnConflictWhenEmailExists() throws Exception {

        RegisterRequest request =
                new RegisterRequest(
                        "John Doe",
                        "john@gmail.com",
                        "password123"
                );

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 404 when user does not exist")
    void shouldReturn404WhenUserNotFound() throws Exception {

        LoginRequest request =
                new LoginRequest(
                        "john@gmail.com",
                        "password123"
                );

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when password is incorrect")
    void shouldReturnUnauthorizedWhenPasswordIncorrect() throws Exception {

        LoginRequest request =
                new LoginRequest(
                        "john@gmail.com",
                        "wrongpassword"
                );

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail validation when register request is invalid")
    void shouldReturn400WhenRegisterRequestIsInvalid() throws Exception {

        RegisterRequest request =
                new RegisterRequest(
                        "",
                        "",
                        ""
                );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail validation when login request is invalid")
    void shouldReturn400WhenLoginRequestIsInvalid() throws Exception {

        LoginRequest request =
                new LoginRequest(
                        "",
                        ""
                );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {

        String invalidJson = """
        {
            "email":"john@gmail.com",
            "password":
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn415WhenContentTypeIsWrong() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }
}