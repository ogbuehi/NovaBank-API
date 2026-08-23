package com.bankapi.NovaBank.API.controller;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
import com.bankapi.NovaBank.API.dto.response.AccountResponse;
import com.bankapi.NovaBank.API.dto.response.TransactionResponse;
import com.bankapi.NovaBank.API.entity.TransactionType;
import com.bankapi.NovaBank.API.exception.AccountNotFoundException;
import com.bankapi.NovaBank.API.exception.InsufficientBalanceException;
import com.bankapi.NovaBank.API.exception.InvalidAmountException;
import com.bankapi.NovaBank.API.service.AccountService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    @WithMockUser(username = "john@gmail.com")
    @DisplayName("Should create account")
    void shouldCreateAccount() throws Exception {

        AccountResponse response = new AccountResponse(
                "successful",
                "1000000001",
                "John Doe",
                BigDecimal.ZERO,
                null
        );

        when(accountService.createAccount(anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/accounts/create"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber")
                        .value("1000000001"))
                .andExpect(jsonPath("$.accountName")
                        .value("John Doe"));

        verify(accountService).createAccount(
                eq("john@gmail.com")
        );
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldGetAccount() throws Exception {

        AccountResponse response = new AccountResponse(
                "successful",
                "1000000001",
                "John Doe",
                BigDecimal.valueOf(5000),
                null
        );

        when(accountService.getAccountByEmail(anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(5000));

        verify(accountService).getAccountByEmail(
                eq("john@gmail.com")
        );
    }
    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldDepositMoney() throws Exception {

        TransactionResponse response =
                new TransactionResponse(
                        1L,
                        null,
                        "1000000001",
                        BigDecimal.valueOf(7000),
                        TransactionType.DEPOSIT,
                        LocalDateTime.now()
                );

        when(accountService.deposit(anyString(), any(BigDecimal.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/accounts/deposit")
                        .param("amount", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(7000))
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));

        verify(accountService).deposit(
                eq("john@gmail.com"),
                eq(BigDecimal.valueOf(2000))
        );
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldWithdrawMoney() throws Exception {

        TransactionResponse response =
                new TransactionResponse(
                        1L,
                        "1000000001",
                        null,
                        BigDecimal.valueOf(3000),
                        TransactionType.WITHDRAW,
                        LocalDateTime.now()
                );

        when(accountService.withdraw(anyString(), any(BigDecimal.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/accounts/withdraw")
                        .param("amount", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(3000))
                .andExpect(jsonPath("$.transactionType").value("WITHDRAW"));

        verify(accountService).withdraw(
                eq("john@gmail.com"),
                eq(BigDecimal.valueOf(2000))
        );
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldGetTransactions() throws Exception {

        TransactionResponse transaction =
                new TransactionResponse(
                        1L,
                        "1000000001",
                        "1000000002",
                        BigDecimal.valueOf(2000),
                        TransactionType.TRANSFER,
                        LocalDateTime.now()
                );

        Page<TransactionResponse> page =
                new PageImpl<>(List.of(transaction));

        when(accountService.getUserTransactions(anyString(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/accounts/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(2000))
                .andExpect(jsonPath("$.content[0].transactionType").value("TRANSFER"));

        verify(accountService).getUserTransactions(
                eq("john@gmail.com"),
                any(Pageable.class)
        );
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingAccountWithoutToken() throws Exception {

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldReturn404WhenAccountNotFound() throws Exception {

        when(accountService.getAccountByEmail(anyString()))
                .thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldReturn400WhenDepositAmountIsInvalid() throws Exception {

        when(accountService.deposit(anyString(), any(BigDecimal.class)))
                .thenThrow(new InvalidAmountException(
                        "Deposit amount must be greater than zero"
                ));

        mockMvc.perform(post("/api/accounts/deposit")
                        .param("amount", "-100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldReturn400WhenWithdrawAmountIsInvalid() throws Exception {

        when(accountService.withdraw(anyString(), any(BigDecimal.class)))
                .thenThrow(new InvalidAmountException(
                        "Withdraw amount must be greater than zero"
                ));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .param("amount", "-500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldReturn400WhenBalanceIsInsufficient() throws Exception {

        when(accountService.withdraw(anyString(), any(BigDecimal.class)))
                .thenThrow(new InsufficientBalanceException(
                        "Insufficient Balance"
                ));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .param("amount", "100000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldReturn404WhenReceiverAccountDoesNotExist() throws Exception {

        when(accountService.transferFunds(
                anyString(),
                anyString(),
                any(BigDecimal.class)
        )).thenThrow(new AccountNotFoundException(
                "Account not found"
        ));

        mockMvc.perform(post("/api/accounts/transfer")
                        .param("receiverAccount", "9999999999")
                        .param("amount", "500"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldReturn400WhenTransferAmountIsInvalid() throws Exception {

        when(accountService.transferFunds(
                anyString(),
                anyString(),
                any(BigDecimal.class)
        )).thenThrow(new InvalidAmountException(
                "Transfer amount must be greater than zero"
        ));

        mockMvc.perform(post("/api/accounts/transfer")
                        .param("receiverAccount", "1000000002")
                        .param("amount", "-50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@gmail.com")
    void shouldReturnEmptyTransactionPage() throws Exception {

        Page<TransactionResponse> page =
                new PageImpl<>(List.of());

        when(accountService.getUserTransactions(
                anyString(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/accounts/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}