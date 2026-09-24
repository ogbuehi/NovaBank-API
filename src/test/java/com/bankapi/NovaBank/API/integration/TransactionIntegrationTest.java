package com.bankapi.NovaBank.API.integration;

import com.bankapi.NovaBank.API.container.AbstractContainerBaseTest;
import com.bankapi.NovaBank.API.dto.request.LoginRequest;
import com.bankapi.NovaBank.API.dto.request.RegisterRequest;
import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.Role;
import com.bankapi.NovaBank.API.entity.Transaction;
import com.bankapi.NovaBank.API.entity.TransactionStatus;
import com.bankapi.NovaBank.API.entity.TransactionType;
import com.bankapi.NovaBank.API.entity.User;
import com.bankapi.NovaBank.API.repository.AccountRepository;
import com.bankapi.NovaBank.API.repository.TransactionRepository;
import com.bankapi.NovaBank.API.repository.UserRepository;
import com.bankapi.NovaBank.API.service.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String johnToken;
    private String janeToken;

    private String johnAccount;
    private String janeAccount;

    @BeforeEach
    void setup() throws Exception {

        // Clean database before every test
        transactionRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Generate unique emails so every test starts with fresh users
        String suffix = UUID.randomUUID().toString();

        String johnEmail = "john-" + suffix + "@gmail.com";
        String janeEmail = "jane-" + suffix + "@gmail.com";

        // Register users
        registerUser("John Doe", johnEmail);
        registerUser("Jane Doe", janeEmail);

        // Login users and obtain JWT tokens
        johnToken = login(johnEmail);
        janeToken = login(janeEmail);

        // Create accounts
        johnAccount = createAccount(johnToken);
        janeAccount = createAccount(janeToken);
    }

    private void registerUser(String name, String email) throws Exception {

        RegisterRequest request = new RegisterRequest(
                name,
                email,
                "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private String login(String email) throws Exception {

        LoginRequest request = new LoginRequest(
                email,
                "password123"
        );

        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readTree(json)
                .get("token")
                .asText();
    }

    private String createAccount(String token) throws Exception {

        String json = mockMvc.perform(post("/api/accounts/create")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = mapper.readTree(json);

        return node.get("accountNumber").asText();
    }

    @Test
    void shouldDepositMoney() throws Exception {

        mockMvc.perform(post("/api/accounts/deposit")
                        .header("Authorization", "Bearer " + johnToken)
                        .param("amount", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.receiverAccount").value(johnAccount));
    }

    @Test
    void shouldWithdrawMoney() throws Exception {

        mockMvc.perform(post("/api/accounts/deposit")
                        .header("Authorization", "Bearer " + johnToken)
                        .param("amount", "10000"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/accounts/withdraw")
                        .header("Authorization", "Bearer " + johnToken)
                        .param("amount", "3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(3000))
                .andExpect(jsonPath("$.transactionType").value("WITHDRAW"))
                .andExpect(jsonPath("$.senderAccount").value(johnAccount));
    }

    @Test
    void shouldTransferFunds() throws Exception {

        mockMvc.perform(post("/api/accounts/deposit")
                .header("Authorization", "Bearer " + johnToken)
                .param("amount", "10000"));

        mockMvc.perform(post("/api/accounts/transfer")
                        .header("Authorization", "Bearer " + johnToken)
                        .param("receiverAccount", janeAccount)
                        .param("amount", "2500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverAccount")
                        .value(janeAccount));
    }

    @Test
    void shouldReturnTransactions() throws Exception {

        mockMvc.perform(post("/api/accounts/deposit")
                .header("Authorization", "Bearer " + johnToken)
                .param("amount", "3000"));

        mockMvc.perform(get("/api/accounts/transactions")
                        .header("Authorization", "Bearer " + johnToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateSuccessfulWithdrawalTransaction() {

        // Create a separate user specifically for this service-level test
        User user = User.builder()
                .fullName("Transaction User")
                .email("transaction@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Give the test account an initial balance without creating a transaction
        Account account = Account.builder()
                .accountNumber("9000000001")
                .accountName("Transaction User")
                .balance(BigDecimal.valueOf(10_000))
                .user(user)
                .build();

        accountRepository.save(account);

        // Perform withdrawal
        accountService.withdraw(
                "transaction@test.com",
                BigDecimal.valueOf(3_000)
        );

        // Verify final balance
        Account updatedAccount = accountRepository
                .findByUserEmail("transaction@test.com")
                .orElseThrow();

        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo("7000");

        // Verify transaction was persisted
        List<Transaction> transactions = transactionRepository.findAll();

        assertThat(transactions)
                .hasSize(1);

        Transaction transaction = transactions.get(0);

        assertThat(transaction.getTransactionReference())
                .isNotBlank();

        assertThat(transaction.getStatus())
                .isEqualTo(TransactionStatus.SUCCESS);

        assertThat(transaction.getTransactionDate())
                .isNotNull();

        assertThat(transaction.getTransactionType())
                .isEqualTo(TransactionType.WITHDRAW);

        assertThat(transaction.getAmount())
                .isEqualByComparingTo("3000");
    }
}