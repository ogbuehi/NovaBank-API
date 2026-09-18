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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    // CREATE ACCOUNT

    public AccountResponse createAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (accountRepository.existsByUserEmail(email)) {
            throw new AccountAlreadyExistsException(
                    "Account already exists"
            );
        }

        Account account = Account.builder()
                .accountName(user.getFullName())
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);

        return accountMapper.toResponse(savedAccount);
    }

    // GET ACCOUNT

    public AccountResponse getAccountByEmail(String email) {

        Account account = accountRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));

        return accountMapper.toResponse(account);
    }

    // INTERNAL USE ONLY
    // Normal read - no database write lock.

    public Account getAccountEntityByEmail(String email) {

        return accountRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));
    }

    // INTERNAL USE ONLY
    // Normal read - no database write lock.

    public Account getAccountEntityByAccountNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));
    }

    // INTERNAL USE ONLY
    // Locked read - used only when modifying account balance.

    private Account getLockedAccountByEmail(String email) {

        return accountRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));
    }

    // INTERNAL USE ONLY
    // Locked read - used for transfer operations.

    private Account getLockedAccountByAccountNumber(
            String accountNumber
    ) {

        return accountRepository.findByAccountNumberForUpdate(
                        accountNumber
                )
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));
    }

    // DEPOSIT

    @Transactional
    public TransactionResponse deposit(
            String email,
            BigDecimal amount
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Deposit amount must be greater than zero"
            );
        }

        /*
         * Lock the account before reading or modifying its balance.
         */
        Account account = getLockedAccountByEmail(email);

        account.setBalance(
                account.getBalance().add(amount)
        );

        Transaction transaction = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .receiverAccount(account)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description("Account deposit")
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    // WITHDRAW

    @Transactional
    public TransactionResponse withdraw(
            String email,
            BigDecimal amount
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Withdrawal amount must be greater than zero"
            );
        }

        /*
         * Lock the account before checking the balance.
         * This prevents concurrent withdrawals from
         * reading the same old balance.
         */
        Account account = getLockedAccountByEmail(email);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }

        account.setBalance(
                account.getBalance().subtract(amount)
        );

        Transaction transaction = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .senderAccount(account)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .description("Account withdrawal")
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    // TRANSFER FUNDS

    @Transactional
    public TransactionResponse transferFunds(
            String senderEmail,
            String receiverAccountNumber,
            BigDecimal amount
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Transfer amount must be greater than zero"
            );
        }

        /*
         * First perform normal reads to identify both accounts.
         * We need their IDs so that we can acquire the write
         * locks in a consistent order.
         */
        Account senderReference = accountRepository
                .findByUserEmail(senderEmail)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Sender account not found"
                        ));

        Account receiverReference = accountRepository
                .findByAccountNumber(receiverAccountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Receiver account not found"
                        ));

        /*
         * Prevent transferring money to the same account.
         */
        if (senderReference.getId()
                .equals(receiverReference.getId())) {

            throw new InvalidAmountException(
                    "Cannot transfer to the same account"
            );
        }

        Account firstLocked;
        Account secondLocked;

        /*
         * Always acquire account locks in the same order.
         *
         * This reduces the possibility of a deadlock when
         * two opposite transfers happen concurrently.
         */
        if (senderReference.getId()
                .compareTo(receiverReference.getId()) < 0) {

            firstLocked = getLockedAccountByAccountNumber(
                    senderReference.getAccountNumber()
            );

            secondLocked = getLockedAccountByAccountNumber(
                    receiverReference.getAccountNumber()
            );

        } else {

            firstLocked = getLockedAccountByAccountNumber(
                    receiverReference.getAccountNumber()
            );

            secondLocked = getLockedAccountByAccountNumber(
                    senderReference.getAccountNumber()
            );
        }

        /*
         * Restore the sender and receiver references to the
         * locked entities.
         */
        Account sender = firstLocked.getId()
                .equals(senderReference.getId())
                ? firstLocked
                : secondLocked;

        Account receiver = firstLocked.getId()
                .equals(receiverReference.getId())
                ? firstLocked
                : secondLocked;

        /*
         * Check the sender's balance only after the sender
         * has been locked.
         */
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }

        sender.setBalance(
                sender.getBalance().subtract(amount)
        );

        receiver.setBalance(
                receiver.getBalance().add(amount)
        );

        Transaction transaction = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description("Account transfer")
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    // GET USER TRANSACTIONS

    public Page<TransactionResponse> getUserTransactions(
            String email,
            Pageable pageable
    ) {

        /*
         * This is a read-only operation, so we intentionally
         * do not acquire a PESSIMISTIC_WRITE lock.
         */
        Account account = getAccountEntityByEmail(email);

        Page<Transaction> transactions =
                transactionRepository
                        .findBySenderAccountOrReceiverAccount(
                                account,
                                account,
                                pageable
                        );

        return transactions.map(transactionMapper::toResponse);
    }

    // GENERATE ACCOUNT NUMBER

    private String generateAccountNumber() {

        return String.valueOf(
                1000000000L +
                        (long) (Math.random() * 9000000000L)
        );
    }

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID();
    }
}