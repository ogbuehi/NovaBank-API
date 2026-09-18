package com.bankapi.NovaBank.API.repository;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
import com.bankapi.NovaBank.API.entity.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TransactionRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return user transactions")
    void shouldFindTransactionsBySenderOrReceiver() {

        User senderUser = userRepository.save(
                User.builder()
                        .fullName("John")
                        .email("john@gmail.com")
                        .password("password")
                        .role(Role.USER)
                        .build()
        );

        User receiverUser = userRepository.save(
                User.builder()
                        .fullName("Jane")
                        .email("jane@gmail.com")
                        .password("password")
                        .role(Role.USER)
                        .build()
        );

        Account sender = accountRepository.save(
                Account.builder()
                        .accountNumber("1000000001")
                        .accountName("John")
                        .balance(BigDecimal.valueOf(5000))
                        .user(senderUser)
                        .build()
        );

        Account receiver = accountRepository.save(
                Account.builder()
                        .accountNumber("1000000002")
                        .accountName("Jane")
                        .balance(BigDecimal.valueOf(2000))
                        .user(receiverUser)
                        .build()
        );

        transactionRepository.save(
                Transaction.builder()
                        .transactionReference("TXN-TEST-001")
                        .receiverAccount(receiver)
                        .amount(BigDecimal.valueOf(1000))
                        .transactionType(TransactionType.TRANSFER)
                        .status(TransactionStatus.SUCCESS)
                        .description("Account Transfer")
                        .transactionDate(LocalDateTime.now())
                        .build()
        );

        Page<Transaction> page =
                transactionRepository.findBySenderAccountOrReceiverAccount(
                        sender,
                        receiver,
                        PageRequest.of(0, 10)
                );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getAmount())
                .isEqualByComparingTo("1000");
    }
}