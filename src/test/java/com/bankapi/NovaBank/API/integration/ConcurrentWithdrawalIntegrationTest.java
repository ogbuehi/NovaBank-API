package com.bankapi.NovaBank.API.integration;

import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.Role;
import com.bankapi.NovaBank.API.entity.User;
import com.bankapi.NovaBank.API.exception.InsufficientBalanceException;
import com.bankapi.NovaBank.API.repository.AccountRepository;
import com.bankapi.NovaBank.API.repository.UserRepository;
import com.bankapi.NovaBank.API.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrentWithdrawalIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private static final String EMAIL = "concurrent@gmail.com";

    @BeforeEach
    void setUp() {

        accountRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .fullName("Concurrent User")
                .email(EMAIL)
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        Account account = Account.builder()
                .accountNumber("9000000001")
                .accountName("Concurrent User")
                .balance(BigDecimal.valueOf(10_000))
                .user(user)
                .build();

        accountRepository.save(account);
    }

    @Test
    void shouldPreventConcurrentWithdrawalsFromOverspending() throws Exception {

        int numberOfWithdrawals = 2;
        BigDecimal withdrawalAmount = BigDecimal.valueOf(7_000);

        ExecutorService executorService =
                Executors.newFixedThreadPool(numberOfWithdrawals);

        CountDownLatch ready = new CountDownLatch(numberOfWithdrawals);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Object>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfWithdrawals; i++) {

            futures.add(
                    executorService.submit(() -> {

                        ready.countDown();

                        start.await();

                        try {
                            return accountService.withdraw(
                                    EMAIL,
                                    withdrawalAmount
                            );
                        } catch (InsufficientBalanceException exception) {
                            return exception;
                        }
                    })
            );
        }

        // Make sure both threads are ready before releasing them.
        assertThat(ready.await(5, TimeUnit.SECONDS))
                .isTrue();

        // Start both withdrawals at approximately the same time.
        start.countDown();

        List<Object> results = new ArrayList<>();

        for (Future<Object> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }

        executorService.shutdown();

        long successfulWithdrawals = results.stream()
                .filter(result -> !(result instanceof InsufficientBalanceException))
                .count();

        long failedWithdrawals = results.stream()
                .filter(result -> result instanceof InsufficientBalanceException)
                .count();

        assertThat(successfulWithdrawals)
                .isEqualTo(1);

        assertThat(failedWithdrawals)
                .isEqualTo(1);

        Account account = accountRepository
                .findByUserEmail(EMAIL)
                .orElseThrow();

        assertThat(account.getBalance())
                .isEqualByComparingTo("3000");
    }
}