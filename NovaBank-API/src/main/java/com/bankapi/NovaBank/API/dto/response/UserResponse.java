package com.bankapi.NovaBank.API.dto.response;

import com.bankapi.NovaBank.API.entity.Role;

public record UserResponse(Long id,

                           String fullName,

                           String email,

                           Role role) {
}
