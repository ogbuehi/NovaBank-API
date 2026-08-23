package com.bankapi.NovaBank.API.repository;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.Role;
import com.bankapi.NovaBank.API.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AccountRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find account by account number")
    void shouldFindByAccountNumber() {

        User user = userRepository.save(
                User.builder()
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .password("password")
                        .role(Role.USER)
                        .build()
        );

        Account account = Account.builder()
                .accountName("John Doe")
                .accountNumber("1000000001")
                .balance(BigDecimal.valueOf(5000))
                .user(user)
                .build();

        accountRepository.save(account);

        assertThat(accountRepository
                .findByAccountNumber("1000000001"))
                .isPresent();
    }

    @Test
    @DisplayName("Should find account by user email")
    void shouldFindByUserEmail() {

        User user = userRepository.save(
                User.builder()
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .password("password")
                        .role(Role.USER)
                        .build()
        );

        accountRepository.save(
                Account.builder()
                        .accountName("John Doe")
                        .accountNumber("1000000001")
                        .balance(BigDecimal.ZERO)
                        .user(user)
                        .build()
        );

        assertThat(accountRepository
                .findByUserEmail("john@gmail.com"))
                .isPresent();
    }

    @Test
    @DisplayName("Should check account existence by email")
    void shouldCheckExistsByUserEmail() {

        User user = userRepository.save(
                User.builder()
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .password("password")
                        .role(Role.USER)
                        .build()
        );

        accountRepository.save(
                Account.builder()
                        .accountName("John Doe")
                        .accountNumber("1000000001")
                        .balance(BigDecimal.ZERO)
                        .user(user)
                        .build()
        );

        assertThat(accountRepository
                .existsByUserEmail("john@gmail.com"))
                .isTrue();
    }
}