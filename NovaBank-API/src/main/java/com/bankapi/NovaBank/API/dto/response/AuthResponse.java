package com.bankapi.NovaBank.API.dto.response;

import com.bankapi.NovaBank.API.entity.Role;

public record AuthResponse(
        String token,
        String email,
        Role role
) {
}
