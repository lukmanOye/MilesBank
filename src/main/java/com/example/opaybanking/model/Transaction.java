package com.example.opaybanking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private int walletId;
    private double amount;
    private TransactionType transactionType;
    private String reference;
    private Status transactionStatus;
    private String description;
    private LocalDateTime createdAt;

}
