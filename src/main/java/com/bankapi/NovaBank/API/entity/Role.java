package com.bankapi.NovaBank.API.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Role",
        description = "Role assigned to a NovaBank user"
)
public enum Role {

    @Schema(description = "Standard bank customer")
    USER,

    @Schema(description = "Bank administrator")
    ADMIN
}