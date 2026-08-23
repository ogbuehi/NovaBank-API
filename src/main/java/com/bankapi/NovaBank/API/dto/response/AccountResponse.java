package com.bankapi.NovaBank.API.dto.response;

import java.math.BigDecimal;

public record AccountResponse(String message,
                              String accountNumber,
                              String accountName,
                              BigDecimal balance,
                              UserResponse user
) {
}
