package com.bankapi.NovaBank.API.repository;

import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUserEmail(String email);
    boolean existsByUserEmail(String email);


}
