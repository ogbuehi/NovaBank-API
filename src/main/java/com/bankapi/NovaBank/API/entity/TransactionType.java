package com.bankapi.NovaBank.API.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TransactionType",
        description = "Type of financial transaction performed on a NovaBank account"
)
public enum TransactionType {

    @Schema(description = "Money deposited into an account")
    DEPOSIT,

    @Schema(description = "Money withdrawn from an account")
    WITHDRAW,

    @Schema(description = "Money transferred between two accounts")
    TRANSFER
}