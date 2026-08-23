package com.bankapi.NovaBank.API.service;

import com.bankapi.NovaBank.API.dto.request.LoginRequest;
import com.bankapi.NovaBank.API.dto.request.RegisterRequest;
import com.bankapi.NovaBank.API.dto.response.AuthResponse;
import com.bankapi.NovaBank.API.entity.Role;
import com.bankapi.NovaBank.API.entity.User;
import com.bankapi.NovaBank.API.exception.UserAlreadyExistsException;
import com.bankapi.NovaBank.API.exception.UserNotFoundException;
import com.bankapi.NovaBank.API.repository.UserRepository;
import com.bankapi.NovaBank.API.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@gmail.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        registerRequest = new RegisterRequest(
                "John Doe",
                "john@gmail.com",
                "password123"
        );

        loginRequest = new LoginRequest(
                "john@gmail.com",
                "password123"
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        String result = authService.register(registerRequest);

        assertThat(result)
                .isEqualTo("User registered successfully");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registerRequest)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldEncodePasswordBeforeSaving() {

        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        authService.register(registerRequest);

        verify(passwordEncoder)
                .encode(registerRequest.getPassword());
    }

    @Test
    void shouldLoginSuccessfully() {

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()))
                .thenReturn(true);

        when(jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()))
                .thenReturn("jwt-token");

        AuthResponse response =
                authService.login(loginRequest);

        assertThat(response.token())
                .isEqualTo("jwt-token");

        assertThat(response.email())
                .isEqualTo(user.getEmail());

        assertThat(response.role())
                .isEqualTo(Role.USER);

        verify(jwtUtil).generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> authService.login(loginRequest)
        );

        verify(jwtUtil, never())
                .generateToken(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        verify(jwtUtil, never())
                .generateToken(any(), any());
    }

    @Test
    void shouldNotGenerateTokenWhenUserNotFound() {

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> authService.login(loginRequest)
        );

        verify(jwtUtil, never())
                .generateToken(any(), any());
    }

    @Test
    void shouldNotGenerateTokenForWrongPassword() {

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        verify(jwtUtil, never())
                .generateToken(any(), any());
    }
}