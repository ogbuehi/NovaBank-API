package com.bankapi.NovaBank.API.service;

import com.bankapi.NovaBank.API.dto.response.AccountResponse;
import com.bankapi.NovaBank.API.dto.response.TransactionResponse;
import com.bankapi.NovaBank.API.entity.*;
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
                .transactionReference("TXN-TEST-001")
                .receiverAccount(account)
                .amount(BigDecimal.valueOf(2000))
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description("Account deposit")
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionResponse response =
                mock(TransactionResponse.class);

        /*
         * Deposit now uses the pessimistic-locking query.
         */
        when(accountRepository.findByUserEmailForUpdate(anyString()))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        TransactionResponse result =
                accountService.deposit(
                        user.getEmail(),
                        BigDecimal.valueOf(2000)
                );

        assertThat(result).isEqualTo(response);

        assertThat(account.getBalance())
                .isEqualByComparingTo("7000");

        verify(accountRepository)
                .findByUserEmailForUpdate(user.getEmail());

        verify(transactionRepository)
                .save(any(Transaction.class));
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

        verify(accountRepository, never())
                .findByUserEmailForUpdate(anyString());
    }

    // ================= WITHDRAW =================

    @Test
    void shouldWithdrawSuccessfully() {

        Transaction transaction = Transaction.builder()
                .transactionReference("TXN-TEST-001")
                .receiverAccount(account)
                .amount(BigDecimal.valueOf(2000))
                .transactionType(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .description("Account withdrawal")
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionResponse response =
                mock(TransactionResponse.class);

        /*
         * Withdrawal must retrieve the account with
         * PESSIMISTIC_WRITE locking.
         */
        when(accountRepository.findByUserEmailForUpdate(anyString()))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        TransactionResponse result =
                accountService.withdraw(
                        user.getEmail(),
                        BigDecimal.valueOf(1000)
                );

        assertThat(result).isEqualTo(response);

        assertThat(account.getBalance())
                .isEqualByComparingTo("4000");

        verify(accountRepository)
                .findByUserEmailForUpdate(user.getEmail());
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

        verify(accountRepository, never())
                .findByUserEmailForUpdate(anyString());
    }

    @Test
    void shouldThrowWhenBalanceInsufficient() {

        when(accountRepository.findByUserEmailForUpdate(anyString()))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(
                        user.getEmail(),
                        BigDecimal.valueOf(6000)
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenWithdrawAccountNotFound() {

        when(accountRepository.findByUserEmailForUpdate(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.withdraw(
                        user.getEmail(),
                        BigDecimal.valueOf(1000)
                )
        );
    }

    // ================= TRANSFER =================

    @Test
    void shouldTransferSuccessfully() {

        Transaction transaction = Transaction.builder()
                .transactionReference("TXN-TEST-001")
                .receiverAccount(account)
                .amount(BigDecimal.valueOf(2000))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description("Account Transfer")
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionResponse response =
                mock(TransactionResponse.class);

        /*
         * Initial reads identify the sender and receiver.
         */
        when(accountRepository.findByUserEmail(user.getEmail()))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumber(
                receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));

        /*
         * The service then obtains pessimistic write locks
         * using account numbers.
         */
        when(accountRepository.findByAccountNumberForUpdate(
                account.getAccountNumber()))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate(
                receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        TransactionResponse result =
                accountService.transferFunds(
                        user.getEmail(),
                        receiver.getAccountNumber(),
                        BigDecimal.valueOf(2000)
                );

        assertThat(result).isEqualTo(response);

        assertThat(account.getBalance())
                .isEqualByComparingTo("3000");

        assertThat(receiver.getBalance())
                .isEqualByComparingTo("5000");

        verify(accountRepository)
                .findByAccountNumberForUpdate(
                        account.getAccountNumber());

        verify(accountRepository)
                .findByAccountNumberForUpdate(
                        receiver.getAccountNumber());
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

        verify(accountRepository, never())
                .findByUserEmail(anyString());
    }

    @Test
    void shouldThrowWhenTransferBalanceInsufficient() {

        when(accountRepository.findByUserEmail(user.getEmail()))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumber(
                receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));

        when(accountRepository.findByAccountNumberForUpdate(
                account.getAccountNumber()))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate(
                receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.transferFunds(
                        user.getEmail(),
                        receiver.getAccountNumber(),
                        BigDecimal.valueOf(10000)
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenTransferReceiverNotFound() {

        when(accountRepository.findByUserEmail(user.getEmail()))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumber(
                receiver.getAccountNumber()))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.transferFunds(
                        user.getEmail(),
                        receiver.getAccountNumber(),
                        BigDecimal.valueOf(1000)
                )
        );
    }

    @Test
    void shouldThrowWhenTransferToSameAccount() {

        when(accountRepository.findByUserEmail(user.getEmail()))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumber(
                account.getAccountNumber()))
                .thenReturn(Optional.of(account));

        assertThrows(
                InvalidAmountException.class,
                () -> accountService.transferFunds(
                        user.getEmail(),
                        account.getAccountNumber(),
                        BigDecimal.valueOf(1000)
                )
        );

        verify(accountRepository, never())
                .findByAccountNumberForUpdate(anyString());
    }

    // ================= TRANSACTIONS =================

    @Test
    void shouldReturnTransactions() {

        Transaction transaction = Transaction.builder()
                .transactionReference("TXN-TEST-001")
                .receiverAccount(account)
                .amount(BigDecimal.valueOf(2000))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description("Account Transfer")
                .transactionDate(LocalDateTime.now())
                .build();

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction));

        /*
         * Transaction history is read-only, so it should
         * continue using the normal account lookup.
         */
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
                        PageRequest.of(0, 10)
                );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}