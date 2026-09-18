package com.bankapi.NovaBank.API.repository;

import com.bankapi.NovaBank.API.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByUserEmail(String email);

    boolean existsByUserEmail(String email);

    /**
     * Retrieves an account by the owner's email and places a pessimistic
     * write lock on the account row for the duration of the transaction.
     *
     * Used by money-changing operations such as deposits and withdrawals.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            JOIN a.user u
            WHERE u.email = :email
            """)
    Optional<Account> findByUserEmailForUpdate(
            @Param("email") String email
    );

    /**
     * Retrieves an account by account number and places a pessimistic
     * write lock on the account row for the duration of the transaction.
     *
     * Used when transferring money between accounts.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            """)
    Optional<Account> findByAccountNumberForUpdate(
            @Param("accountNumber") String accountNumber
    );
}