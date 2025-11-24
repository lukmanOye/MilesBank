// src/main/java/com/example/opaybanking/model/ExchangeRate.java
package com.example.opaybanking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "exchange_rates")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NGN to USD
    private Double ngnToUsd = 1650.0;

    // Crypto rates (NGN per unit)
    private Double btcToNgn = 110_000_000.0;  // ₦110M per BTC
    private Double ethToNgn = 5_500_000.0;    // ₦5.5M per ETH
    private Double usdtToNgn = 1650.0;        // 1 USDT = ₦1650
    private Double bnbToNgn = 950_000.0;
    private Double solToNgn = 250_000.0;
    private Double dogeToNgn = 220.0;
    private Double xrpToNgn = 900.0;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getNgnToUsd() {
        return ngnToUsd;
    }

    public void setNgnToUsd(Double ngnToUsd) {
        this.ngnToUsd = ngnToUsd;
    }

    public Double getBtcToNgn() {
        return btcToNgn;
    }

    public void setBtcToNgn(Double btcToNgn) {
        this.btcToNgn = btcToNgn;
    }

    public Double getEthToNgn() {
        return ethToNgn;
    }

    public void setEthToNgn(Double ethToNgn) {
        this.ethToNgn = ethToNgn;
    }

    public Double getUsdtToNgn() {
        return usdtToNgn;
    }

    public void setUsdtToNgn(Double usdtToNgn) {
        this.usdtToNgn = usdtToNgn;
    }

    public Double getBnbToNgn() {
        return bnbToNgn;
    }

    public void setBnbToNgn(Double bnbToNgn) {
        this.bnbToNgn = bnbToNgn;
    }

    public Double getSolToNgn() {
        return solToNgn;
    }

    public void setSolToNgn(Double solToNgn) {
        this.solToNgn = solToNgn;
    }

    public Double getDogeToNgn() {
        return dogeToNgn;
    }

    public void setDogeToNgn(Double dogeToNgn) {
        this.dogeToNgn = dogeToNgn;
    }

    public Double getXrpToNgn() {
        return xrpToNgn;
    }

    public void setXrpToNgn(Double xrpToNgn) {
        this.xrpToNgn = xrpToNgn;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}