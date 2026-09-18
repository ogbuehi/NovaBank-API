package com.bankapi.NovaBank.API.controller;

import com.bankapi.NovaBank.API.dto.request.LoginRequest;
import com.bankapi.NovaBank.API.dto.request.RegisterRequest;
import com.bankapi.NovaBank.API.dto.response.AuthResponse;
import com.bankapi.NovaBank.API.dto.response.RegisterResponse;
import com.bankapi.NovaBank.API.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(
        name = "Authentication",
        description = "User registration and authentication endpoints"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new NovaBank user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "User registered successfully"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with the provided email already exists",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid
            @RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "fullName": "John Doe",
                                              "email": "john@gmail.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.ok(
                new RegisterResponse("User registered successfully")
        );
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates a registered user and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzUxMiJ9...",
                                              "email": "john@gmail.com",
                                              "role": "USER"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid login data",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "email": "john@gmail.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}