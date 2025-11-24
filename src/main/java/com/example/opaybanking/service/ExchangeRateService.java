package com.example.opaybanking.service;

import com.example.opaybanking.model.ExchangeRate;
import com.example.opaybanking.repo.ExchangeRateRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepo repo;

    public ExchangeRateService(ExchangeRateRepo repo) {
        this.repo = repo;
    }


    public ExchangeRate getRate() {
        return repo.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    ExchangeRate rate = new ExchangeRate();
                    return repo.save(rate);
                });
    }

    public ExchangeRate updateRate(Map<String, Double> rates) {
        ExchangeRate rate = getRate();
        rates.forEach((key, value) -> {
            switch (key.toLowerCase()) {
                case "ngn_usd" -> rate.setNgnToUsd(value);
                case "btc" -> rate.setBtcToNgn(value);
                case "eth" -> rate.setEthToNgn(value);
                case "usdt" -> rate.setUsdtToNgn(value);
                case "bnb" -> rate.setBnbToNgn(value);
                case "sol" -> rate.setSolToNgn(value);
                case "doge" -> rate.setDogeToNgn(value);
                case "xrp" -> rate.setXrpToNgn(value);
            }
        });
        rate.setUpdatedAt(LocalDateTime.now());
        return repo.save(rate);
    }

    public void deleteRate(Long id) {
        repo.deleteById(id);
    }

    public Map<String, Object> getAllRates() {
        ExchangeRate r = getRate();
        return Map.of(
                "NGN", Map.of("symbol", "₦", "name", "Nigerian Naira"),
                "USD", Map.of("symbol", "$", "name", "US Dollar", "rate", r.getNgnToUsd()),
                "BTC", Map.of("symbol", "₿", "name", "Bitcoin", "rate", String.format("₦%,.0f", r.getBtcToNgn())),
                "ETH", Map.of("symbol", "Ξ", "name", "Ethereum", "rate", String.format("₦%,.0f", r.getEthToNgn())),
                "USDT", Map.of("symbol", "₮", "name", "Tether", "rate", String.format("₦%,.2f", r.getUsdtToNgn())),
                "BNB", Map.of("symbol", "BNB", "name", "Binance Coin", "rate", String.format("₦%,.0f", r.getBnbToNgn())),
                "SOL", Map.of("symbol", "◎", "name", "Solana", "rate", String.format("₦%,.0f", r.getSolToNgn())),
                "DOGE", Map.of("symbol", "Ð", "name", "Dogecoin", "rate", String.format("₦%,.2f", r.getDogeToNgn())),
                "XRP", Map.of("symbol", "XRP", "name", "Ripple", "rate", String.format("₦%,.2f", r.getXrpToNgn())),
                "lastUpdated", r.getUpdatedAt().toString()
        );
    }
}