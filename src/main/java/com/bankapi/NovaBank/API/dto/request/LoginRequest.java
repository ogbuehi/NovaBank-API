package com.bankapi.NovaBank.API.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "LoginRequest",
        description = "Credentials required to authenticate a NovaBank user"
)
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(
            description = "Registered email address of the user",
            example = "john@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "User's account password",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    private String password;
}