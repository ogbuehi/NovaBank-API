package com.bankapi.NovaBank.API.controller;

import com.bankapi.NovaBank.API.dto.response.AccountResponse;
import com.bankapi.NovaBank.API.dto.response.TransactionResponse;
import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.Transaction;
import com.bankapi.NovaBank.API.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // CREATE ACCOUNT
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
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            Authentication authentication,
            @RequestParam BigDecimal amount
    ) {

        String email = authentication.getName();
        System.out.println("Authenticated user: " + authentication.getName());


        return ResponseEntity.ok(
                accountService.deposit(email, amount)
        );
    }

    // WITHDRAW
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            Authentication authentication,
            @RequestParam BigDecimal amount
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                accountService.withdraw(email, amount)
        );
    }

    // TRANSFER FUNDS
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transferFunds(
            Authentication authentication,
            @RequestParam String receiverAccount,
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
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            Authentication authentication,
            Pageable pageable
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                accountService.getUserTransactions(email, pageable)
        );
    }
}
