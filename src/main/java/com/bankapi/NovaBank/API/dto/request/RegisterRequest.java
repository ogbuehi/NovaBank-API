package com.bankapi.NovaBank.API.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "RegisterRequest",
        description = "Information required to register a new NovaBank user"
)
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Schema(
            description = "Full name of the user",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(
            description = "Valid email address for the new user",
            example = "john@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "email"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(
            description = "Password for the new account. Must contain at least 6 characters.",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password",
            minLength = 6
    )
    private String password;
}