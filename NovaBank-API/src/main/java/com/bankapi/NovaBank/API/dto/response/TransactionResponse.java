package com.bankapi.NovaBank.API.dto.response;

import com.bankapi.NovaBank.API.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String senderAccount,
        String receiverAccount,
        BigDecimal amount,
        TransactionType transactionType,
        LocalDateTime transactionDate
) {
}