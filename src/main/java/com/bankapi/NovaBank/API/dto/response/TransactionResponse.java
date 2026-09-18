package com.bankapi.NovaBank.API.dto.response;

import com.bankapi.NovaBank.API.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "TransactionResponse",
        description = "Details of a NovaBank financial transaction"
)
public record TransactionResponse(

        @Schema(
                description = "Unique identifier of the transaction",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Account number from which funds were sent. Null for deposits.",
                example = "1234567890",
                nullable = true
        )
        String senderAccount,

        @Schema(
                description = "Account number that received the funds. Null for withdrawals.",
                example = "0987654321",
                nullable = true
        )
        String receiverAccount,

        @Schema(
                description = "Amount involved in the transaction",
                example = "5000.00"
        )
        BigDecimal amount,

        @Schema(
                description = "Type of financial transaction",
                example = "DEPOSIT"
        )
        TransactionType transactionType,

        @Schema(
                description = "Date and time when the transaction was created",
                example = "2026-08-25T20:30:00"
        )
        LocalDateTime transactionDate
) {
}