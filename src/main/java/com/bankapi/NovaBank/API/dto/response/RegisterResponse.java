package com.bankapi.NovaBank.API.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RegisterResponse",
        description = "Response returned after successful user registration"
)
public record RegisterResponse(

        @Schema(
                description = "Message describing the result of the registration",
                example = "User registered successfully"
        )
        String message
) {
}