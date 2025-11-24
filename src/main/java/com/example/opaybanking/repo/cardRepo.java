package com.example.opaybanking.repo;

import com.example.opaybanking.enums.Currency;
import com.example.opaybanking.model.Card;
import com.example.opaybanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface cardRepo extends JpaRepository<Card, Long> {


    Optional<Card> findByUserAndCurrencyAndIsActiveTrue(User user, Currency currency);

    Optional<Card> findByCardNumberAndIsActiveTrue(String cardNumber);
}