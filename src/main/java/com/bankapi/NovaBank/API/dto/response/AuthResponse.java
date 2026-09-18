package com.bankapi.NovaBank.API.dto.response;

import com.bankapi.NovaBank.API.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AuthResponse",
        description = "Response returned after successful user authentication"
)
public record AuthResponse(

        @Schema(
                description = "JWT access token used to authenticate protected API requests",
                example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huQGdtYWlsLmNvbSIsInJvbGUiOiJVU0VSIn0..."
        )
        String token,

        @Schema(
                description = "Email address of the authenticated user",
                example = "john@gmail.com",
                format = "email"
        )
        String email,

        @Schema(
                description = "Role assigned to the authenticated user",
                example = "USER"
        )
        Role role
) {
}