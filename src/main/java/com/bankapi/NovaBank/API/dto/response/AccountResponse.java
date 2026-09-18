package com.bankapi.NovaBank.API.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        name = "AccountResponse",
        description = "Account information returned by NovaBank account operations"
)
public record AccountResponse(

        @Schema(
                description = "Optional message describing the result of the operation",
                example = "Account created successfully"
        )
        String message,

        @Schema(
                description = "Unique bank account number",
                example = "1234567890"
        )
        String accountNumber,

        @Schema(
                description = "Name associated with the bank account",
                example = "John Doe"
        )
        String accountName,

        @Schema(
                description = "Current account balance",
                example = "50000.00"
        )
        BigDecimal balance,

        @Schema(
                description = "User who owns the account"
        )
        UserResponse user
) {
}