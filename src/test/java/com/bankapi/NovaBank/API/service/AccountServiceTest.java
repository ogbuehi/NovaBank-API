package com.bankapi.NovaBank.API.service;

import com.bankapi.NovaBank.API.dto.response.AccountResponse;
import com.bankapi.NovaBank.API.dto.response.TransactionResponse;
import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.Transaction;
import com.bankapi.NovaBank.API.entity.TransactionType;
import com.bankapi.NovaBank.API.entity.User;
import com.bankapi.NovaBank.API.exception.AccountAlreadyExistsException;
import com.bankapi.NovaBank.API.exception.AccountNotFoundException;
import com.bankapi.NovaBank.API.exception.InsufficientBalanceException;
import com.bankapi.NovaBank.API.exception.InvalidAmountException;
import com.bankapi.NovaBank.API.exception.UserNotFoundException;
import com.bankapi.NovaBank.API.mapper.AccountMapper;
import com.bankapi.NovaBank.API.mapper.TransactionMapper;
import com.bankapi.NovaBank.API.repository.AccountRepository;
import com.bankapi.NovaBank.API.repository.TransactionRepository;
import com.bankapi.NovaBank.API.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;
    private Account receiver;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@gmail.com")
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber("1000000001")
                .accountName("John Doe")
                .balance(BigDecimal.valueOf(5000))
                .user(user)
                .build();

        receiver = Account.builder()
                .id(2L)
                .accountNumber("1000000002")
                .accountName("Jane Doe")
                .balance(BigDecimal.valueOf(3000))
                .build();
    }

    // ================= CREATE ACCOUNT =================

    @Test
    void shouldCreateAccountSuccessfully() {

        AccountResponse response = new AccountResponse(
                "Created",
                "1000000001",
                "John Doe",
                BigDecimal.ZERO,
                null
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.existsByUserEmail(user.getEmail()))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(accountMapper.toResponse(any(Account.class)))
                .thenReturn(response);

        AccountResponse result =
                accountService.createAccount(user.getEmail());

        assertThat(result).isEqualTo(response);

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> accountService.createAccount("john@gmail.com")
        );
    }

    @Test
    void shouldThrowWhenAccountAlreadyExists() {

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(accountRepository.existsByUserEmail(anyString()))
                .thenReturn(true);

        assertThrows(
                AccountAlreadyExistsException.class,
                () -> accountService.createAccount(user.getEmail())
        );
    }

    // ================= GET ACCOUNT =================

    @Test
    void shouldReturnAccountByEmail() {

        AccountResponse response = new AccountResponse(
                "",
                "1000000001",
                "John Doe",
                BigDecimal.valueOf(5000),
                null
        );

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.of(account));

        when(accountMapper.toResponse(account))
                .thenReturn(response);

        AccountResponse result =
                accountService.getAccountByEmail(user.getEmail());

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenAccountNotFound() {

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountByEmail(user.getEmail())
        );
    }

    // ================= DEPOSIT =================

    @Test
    void shouldDepositSuccessfully() {

        Transaction transaction = Transaction.builder()
                .receiverAccount(account)
                .amount(BigDecimal.valueOf(2000))
                .transactionType(TransactionType.DEPOSIT)
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionResponse response =
                mock(TransactionResponse.class);

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        accountService.deposit(
                user.getEmail(),
                BigDecimal.valueOf(2000)
        );

        assertThat(account.getBalance())
                .isEqualByComparingTo("7000");
    }

    @Test
    void shouldThrowWhenDepositAmountInvalid() {

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.deposit(
                        user.getEmail(),
                        BigDecimal.ZERO
                )
        );
    }

    // ================= WITHDRAW =================

    @Test
    void shouldWithdrawSuccessfully() {

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .amount(BigDecimal.valueOf(1000))
                .transactionType(TransactionType.WITHDRAW)
                .transactionDate(LocalDateTime.now())
                .build();

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(mock(TransactionResponse.class));

        accountService.withdraw(
                user.getEmail(),
                BigDecimal.valueOf(1000)
        );

        assertThat(account.getBalance())
                .isEqualByComparingTo("4000");
    }

    @Test
    void shouldThrowWhenWithdrawAmountInvalid() {

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.withdraw(
                        user.getEmail(),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldThrowWhenBalanceInsufficient() {

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(
                        user.getEmail(),
                        BigDecimal.valueOf(6000)
                )
        );
    }

    // ================= TRANSFER =================

    @Test
    void shouldTransferSuccessfully() {

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .receiverAccount(receiver)
                .amount(BigDecimal.valueOf(2000))
                .transactionType(TransactionType.TRANSFER)
                .build();

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(mock(TransactionResponse.class));

        accountService.transferFunds(
                user.getEmail(),
                receiver.getAccountNumber(),
                BigDecimal.valueOf(2000)
        );

        assertThat(account.getBalance())
                .isEqualByComparingTo("3000");

        assertThat(receiver.getBalance())
                .isEqualByComparingTo("5000");
    }

    @Test
    void shouldThrowWhenTransferAmountInvalid() {

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.transferFunds(
                        user.getEmail(),
                        receiver.getAccountNumber(),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldThrowWhenTransferBalanceInsufficient() {

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.transferFunds(
                        user.getEmail(),
                        receiver.getAccountNumber(),
                        BigDecimal.valueOf(10000)
                )
        );
    }

    // ================= TRANSACTIONS =================

    @Test
    void shouldReturnTransactions() {

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .receiverAccount(receiver)
                .amount(BigDecimal.valueOf(500))
                .transactionType(TransactionType.TRANSFER)
                .build();

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction));

        when(accountRepository.findByUserEmail(anyString()))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findBySenderAccountOrReceiverAccount(
                eq(account),
                eq(account),
                any()))
                .thenReturn(page);

        when(transactionMapper.toResponse(any(Transaction.class)))
                .thenReturn(mock(TransactionResponse.class));

        Page<TransactionResponse> result =
                accountService.getUserTransactions(
                        user.getEmail(),
                        PageRequest.of(0,10)
                );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}