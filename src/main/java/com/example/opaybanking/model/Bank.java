// src/main/java/com/example/opaybanking/model/Bank.java
package com.example.opaybanking.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String bankCode;

    @Column(nullable = false)
    private String bankName;

    private String bankLogo;

    @OneToMany(mappedBy = "bank", fetch = FetchType.LAZY)
    private List<Wallet> wallets;

    public Bank() {
    }

    public Bank(String bankCode, String bankName, String bankLogo) {
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.bankLogo = bankLogo;
    }

    // Getters and Setters (you can keep them or use Lombok @Getter/@Setter if you want)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankLogo() { return bankLogo; }
    public void setBankLogo(String bankLogo) { this.bankLogo = bankLogo; }

    public List<Wallet> getWallets() { return wallets; }
    public void setWallets(List<Wallet> wallets) { this.wallets = wallets; }
}