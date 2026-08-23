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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (accountRepository.existsByUserEmail(email)) {
            throw new AccountAlreadyExistsException("Account already exists");
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
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        return accountMapper.toResponse(account);
    }

    // INTERNAL USE ONLY
    public Account getAccountEntityByEmail(String email) {

        return accountRepository.findByUserEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    // INTERNAL USE ONLY
    public Account getAccountEntityByAccountNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    // DEPOSIT
    @Transactional
    public TransactionResponse deposit(String email, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        Account account = getAccountEntityByEmail(email);

        account.setBalance(account.getBalance().add(amount));

        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .receiverAccount(account)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    // WITHDRAW
    @Transactional
    public TransactionResponse withdraw(String email, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        Account account = getAccountEntityByEmail(email);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAW)
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    // TRANSFER
    @Transactional
    public TransactionResponse transferFunds(
            String senderEmail,
            String receiverAccountNumber,
            BigDecimal amount
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        Account sender = getAccountEntityByEmail(senderEmail);

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        Account receiver = getAccountEntityByAccountNumber(receiverAccountNumber);

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    // GET USER TRANSACTIONS
    public Page<TransactionResponse> getUserTransactions(
            String email,
            Pageable pageable
    ) {

        Account account = getAccountEntityByEmail(email);

        Page<Transaction> transactions =
                transactionRepository.findBySenderAccountOrReceiverAccount(
                        account,
                        account,
                        pageable
                );

        return transactions.map(transactionMapper::toResponse);
    }

    private String generateAccountNumber() {

        return String.valueOf(
                1000000000L +
                        (long) (Math.random() * 9000000000L)
        );
    }
}

