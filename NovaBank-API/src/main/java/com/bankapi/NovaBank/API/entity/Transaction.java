package com.bankapi.NovaBank.API.entity;

import com.bankapi.NovaBank.API.entity.Account;
import com.bankapi.NovaBank.API.entity.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionReference;

    @ManyToOne
    private Account senderAccount;

    @ManyToOne
    private Account receiverAccount;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String description;

    private LocalDateTime transactionDate;
}