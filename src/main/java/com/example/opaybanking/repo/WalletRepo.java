package com.example.opaybanking.repo;

import com.example.opaybanking.enums.Currency;
import com.example.opaybanking.model.User;
import com.example.opaybanking.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepo extends JpaRepository<Wallet, Integer> {

    List<Wallet> findByUser(User user);

    boolean existsByUserAndCurrency(User user, Currency currency);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Wallet> findByAccountNumber(String accountNumber);

    // Correct way: Return first wallet (oldest) by user + currency
    @Query("SELECT w FROM Wallet w WHERE w.user = :user AND w.currency = :currency ORDER BY w.walletId ASC")
    List<Wallet> findByUserAndCurrencyOrdered(@Param("user") User user, @Param("currency") Currency currency);

    // Helper: Get first wallet safely
    default Optional<Wallet> findFirstByUserAndCurrency(User user, Currency currency) {
        return findByUserAndCurrencyOrdered(user, currency).stream().findFirst();
    }

    Optional<Wallet> findByUserAndCurrency(User user, Currency currency);
}