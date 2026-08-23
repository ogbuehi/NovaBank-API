package com.bankapi.NovaBank.API.repository;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {

        User user = User.builder()
                .fullName("John Doe")
                .email("john@gmail.com")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        Optional<User> found =
                userRepository.findByEmail("john@gmail.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail())
                .isEqualTo("john@gmail.com");
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldReturnTrueWhenEmailExists() {

        User user = User.builder()
                .fullName("John Doe")
                .email("john@gmail.com")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("john@gmail.com"))
                .isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {

        assertThat(userRepository.existsByEmail("unknown@gmail.com"))
                .isFalse();
    }
}