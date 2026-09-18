package com.bankapi.NovaBank.API.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "ErrorResponse",
        description = "Standard error response returned when an API request fails"
)
public record ErrorResponse(

        @Schema(
                description = "Date and time when the error occurred",
                example = "2026-08-25T21:30:00"
        )
        LocalDateTime timestamp,

        @Schema(
                description = "HTTP status code associated with the error",
                example = "400"
        )
        int status,

        @Schema(
                description = "Short description of the HTTP error",
                example = "Bad Request"
        )
        String error,

        @Schema(
                description = "Detailed explanation of the error",
                example = "Password must be at least 6 characters"
        )
        String message,

        @Schema(
                description = "API endpoint where the error occurred",
                example = "/api/auth/register"
        )
        String path
) {
}