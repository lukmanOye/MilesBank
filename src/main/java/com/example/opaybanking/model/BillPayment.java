package com.example.opaybanking.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int billPaymentId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private double amount;
    private BillType billType;
    private String reference;
    private Status transactionStatus;
    private String details;
    private LocalDateTime createdAt;
}
