package com.bankapi.NovaBank.API.dto.response;

import com.bankapi.NovaBank.API.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "UserResponse",
        description = "Basic information about a NovaBank user"
)
public record UserResponse(

        @Schema(
                description = "Unique identifier of the user",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Full name of the user",
                example = "John Doe"
        )
        String fullName,

        @Schema(
                description = "Email address of the user",
                example = "john@gmail.com",
                format = "email"
        )
        String email,

        @Schema(
                description = "Role assigned to the user",
                example = "USER"
        )
        Role role
) {
}