package com.bankapi.NovaBank.API.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "TransactionPageResponse",
        description = "Paginated transaction history response"
)
public record TransactionPageResponse(

        @Schema(
                description = "Transactions returned for the current page"
        )
        List<TransactionResponse> content,

        @Schema(
                description = "Current page number, starting from 0",
                example = "0"
        )
        int page,

        @Schema(
                description = "Number of transactions requested per page",
                example = "10"
        )
        int size,

        @Schema(
                description = "Total number of transactions available",
                example = "25"
        )
        long totalElements,

        @Schema(
                description = "Total number of pages available",
                example = "3"
        )
        int totalPages
) {
}