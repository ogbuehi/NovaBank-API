package com.bankapi.NovaBank.API.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Used by JPA/Hibernate for optimistic locking.
     * Prevents stale account updates from silently overwriting
     * changes made by another transaction.
     */
    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}