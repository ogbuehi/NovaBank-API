package com.bankapi.NovaBank.API.exception;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
import com.bankapi.NovaBank.API.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest extends AbstractContainerBaseTest {

    private GlobalExceptionHandler handler;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {

        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/accounts/create");
    }

    @Test
    @DisplayName("Should handle AccountNotFoundException")
    void shouldHandleAccountNotFoundException() {

        ResponseEntity<ErrorResponse> response =
                handler.handleAccountNotFound(
                        new AccountNotFoundException("Account not found"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody().message())
                .isEqualTo("Account not found");

        assertThat(response.getBody().path())
                .isEqualTo("/api/accounts/create");
    }

    @Test
    @DisplayName("Should handle UserNotFoundException")
    void shouldHandleUserNotFoundException() {

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFound(
                        new UserNotFoundException("User not found"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody().message())
                .isEqualTo("User not found");
    }

    @Test
    @DisplayName("Should handle AccountAlreadyExistsException")
    void shouldHandleAccountAlreadyExistsException() {

        ResponseEntity<ErrorResponse> response =
                handler.handleAccountAlreadyExists(
                        new AccountAlreadyExistsException("Account already exists"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody().message())
                .isEqualTo("Account already exists");
    }

    @Test
    @DisplayName("Should handle InvalidAmountException")
    void shouldHandleInvalidAmountException() {

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidAmount(
                        new InvalidAmountException("Invalid amount"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody().message())
                .isEqualTo("Invalid amount");
    }

    @Test
    @DisplayName("Should handle InsufficientBalanceException")
    void shouldHandleInsufficientBalanceException() {

        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientBalance(
                        new InsufficientBalanceException("Insufficient balance"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody().message())
                .isEqualTo("Insufficient balance");
    }

    @Test
    @DisplayName("Should handle RuntimeException")
    void shouldHandleRuntimeException() {

        ResponseEntity<ErrorResponse> response =
                handler.handleRuntimeException(
                        new RuntimeException("Runtime error"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody().message())
                .isEqualTo("Runtime error");
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {

        ResponseEntity<ErrorResponse> response =
                handler.handleException(
                        new Exception("Something happened"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody().message())
                .isEqualTo("An unexpected error occurred.");
    }

    @Test
    @DisplayName("Should handle validation errors")
    void shouldHandleValidationException() {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        bindingResult.addError(
                new FieldError(
                        "object",
                        "email",
                        "Email is required"
                )
        );

        bindingResult.addError(
                new FieldError(
                        "object",
                        "password",
                        "Password is required"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        null,
                        bindingResult
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody().message())
                .contains("Email is required");

        assertThat(response.getBody().message())
                .contains("Password is required");
    }

    @Test
    @DisplayName("Should populate all ErrorResponse fields")
    void shouldPopulateEntireErrorResponse() {

        ResponseEntity<ErrorResponse> response =
                handler.handleAccountNotFound(
                        new AccountNotFoundException("Account not found"),
                        request
                );

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.timestamp())
                .isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(body.status())
                .isEqualTo(404);

        assertThat(body.error())
                .isEqualTo("Not Found");

        assertThat(body.message())
                .isEqualTo("Account not found");

        assertThat(body.path())
                .isEqualTo("/api/accounts/create");
    }
}