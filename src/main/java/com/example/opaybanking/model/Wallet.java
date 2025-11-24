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
public class wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int walletId;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    private double balance;
    private String accountNumber;
    private String accountName;
    private String pin;
    private LocalDateTime createdAt;

}
