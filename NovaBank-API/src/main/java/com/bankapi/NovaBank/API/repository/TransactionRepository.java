package com.bankapi.NovaBank.API.repository;

import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findBySenderAccountOrReceiverAccount(
            Account senderAccount,
            Account receiverAccount,
            Pageable pageable
    );
}
