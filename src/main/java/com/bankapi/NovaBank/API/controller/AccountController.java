package com.bankapi.NovaBank.API.controller;

import com.bankapi.NovaBank.API.dto.response.AccountResponse;
import com.bankapi.NovaBank.API.dto.response.TransactionPageResponse;
import com.bankapi.NovaBank.API.dto.response.TransactionResponse;
import com.bankapi.NovaBank.API.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;


import java.math.BigDecimal;

@Tag(
        name = "Accounts",
        description = "Account creation and account management operations"
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    // CREATE ACCOUNT

    @Operation(
            summary = "Create bank account",
            description = "Creates a bank account for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already has an account",
                    content = @Content
            )
    })
    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                accountService.createAccount(email)
        );
    }

    // GET ACCOUNT

    @Operation(
            summary = "Get my account",
            description = "Retrieves the bank account belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content
            )
    })
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getAccount(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                accountService.getAccountByEmail(email)
        );
    }

    // DEPOSIT

    @Operation(
            summary = "Deposit money",
            description = "Deposits the specified amount into the authenticated user's account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposit completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "senderAccount": null,
                                              "receiverAccount": "1234567890",
                                              "amount": 5000,
                                              "transactionType": "DEPOSIT",
                                              "transactionDate": "2026-08-25T20:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid deposit amount",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content
            )
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            Authentication authentication,

            @Parameter(
                    description = "Amount to deposit",
                    example = "5000",
                    required = true
            )
            @RequestParam BigDecimal amount
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                accountService.deposit(email, amount)
        );
    }

    // WITHDRAW

    @Operation(
            summary = "Withdraw money",
            description = "Withdraws the specified amount from the authenticated user's account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 2,
                                              "senderAccount": "1234567890",
                                              "receiverAccount": null,
                                              "amount": 3000,
                                              "transactionType": "WITHDRAW",
                                              "transactionDate": "2026-08-25T20:35:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid amount or insufficient balance",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content
            )
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            Authentication authentication,

            @Parameter(
                    description = "Amount to withdraw",
                    example = "3000",
                    required = true
            )
            @RequestParam BigDecimal amount
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                accountService.withdraw(email, amount)
        );
    }

    // TRANSFER FUNDS

    @Operation(
            summary = "Transfer funds",
            description = "Transfers money from the authenticated user's account to another bank account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 3,
                                              "senderAccount": "1234567890",
                                              "receiverAccount": "0987654321",
                                              "amount": 2500,
                                              "transactionType": "TRANSFER",
                                              "transactionDate": "2026-08-25T20:40:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid amount, insufficient balance, or invalid transfer request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sender or receiver account not found",
                    content = @Content
            )
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transferFunds(
            Authentication authentication,

            @Parameter(
                    description = "Account number of the transfer recipient",
                    example = "0987654321",
                    required = true
            )
            @RequestParam String receiverAccount,

            @Parameter(
                    description = "Amount to transfer",
                    example = "2500",
                    required = true
            )
            @RequestParam BigDecimal amount
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                accountService.transferFunds(
                        email,
                        receiverAccount,
                        amount
                )
        );
    }

    // GET ALL TRANSACTIONS

    @Operation(
            summary = "Get transaction history",
            description = "Returns a paginated list of transactions belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TransactionPageResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content
            )
    })
    @GetMapping("/transactions")
    public ResponseEntity<TransactionPageResponse> getTransactions(
            Authentication authentication,

            @Parameter(
                    description = "Page number, starting from 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Number of transactions to return per page",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int size,

            @Parameter(
                    description = "Field to sort by",
                    example = "transactionDate"
            )
            @RequestParam(defaultValue = "transactionDate") String sort
    ) {
        String email = authentication.getName();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sort)
        );

        Page<TransactionResponse> transactions =
                accountService.getUserTransactions(email, pageable);

        TransactionPageResponse response =
                new TransactionPageResponse(
                        transactions.getContent(),
                        transactions.getNumber(),
                        transactions.getSize(),
                        transactions.getTotalElements(),
                        transactions.getTotalPages()
                );

        return ResponseEntity.ok(response);
    }
}