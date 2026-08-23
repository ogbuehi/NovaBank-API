package com.bankapi.NovaBank.API.controller;

import com.bankapi.NovaBank.API.dto.response.AuthResponse;
import com.bankapi.NovaBank.API.dto.request.LoginRequest;
import com.bankapi.NovaBank.API.dto.request.RegisterRequest;
import com.bankapi.NovaBank.API.dto.response.RegisterResponse;
import com.bankapi.NovaBank.API.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok( new RegisterResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
