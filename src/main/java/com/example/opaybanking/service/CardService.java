package com.example.opaybanking.service;

import com.example.opaybanking.dto.*;
import com.example.opaybanking.enums.Currency;
import com.example.opaybanking.model.Card;
import com.example.opaybanking.model.User;
import com.example.opaybanking.model.Wallet;
import com.example.opaybanking.repo.cardRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
public class CardService {

    private final cardRepo cardRepository;
    private final userService userService;
    private final WalletService walletService;

    private final SecureRandom random = new SecureRandom();
    private static final String MILES_BANK_BIN = "190909";

    public CardService(cardRepo cardRepository, userService userService, WalletService walletService) {
        this.cardRepository = cardRepository;
        this.userService = userService;
        this.walletService = walletService;
    }

    @Transactional
    public CreateCardResponse createVirtualCard(CreateCardRequest req, String token, Currency currency) {
        User user = userService.getAuthenticatedUser(token);

        cardRepository.findByUserAndCurrencyAndIsActiveTrue(user, currency)
                .ifPresent(old -> {
                    old.setActive(false);
                    old.setRevokedAt(LocalDateTime.now());
                    cardRepository.save(old);
                });

        String cardNumber = generateMilesBankCard();
        String expiry = generateExpiry();
        String cvv = generateCVV();

        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(cardNumber);
        card.setCardType(req.cardType());
        card.setCurrency(currency);
        card.setExpiryDate(expiry);
        card.setCvv(cvv);
        card.setPin(req.pin());
        card.setActive(true);
        card.setCreatedAt(LocalDateTime.now());
        cardRepository.save(card);

        String fullName = user.getFirstName() + " " + user.getLastName();

        return new CreateCardResponse(
                true,
                "Virtual " + currency + " card created successfully",
                cardNumber,
                expiry,
                cvv,
                req.pin(),
                req.cardType().name(),
                currency.name(),
                fullName
        );
    }

    @Transactional
    public TopUpResponse topUpWithNgnCard(TopUpFromMilesCardRequest req, String token) {
        User receiver = userService.getAuthenticatedUser(token);
        String cleanCardNo = req.cardNumber().replaceAll("\\D", "");

        Card card = cardRepository.findByCardNumberAndIsActiveTrue(cleanCardNo)
                .orElseThrow(() -> new RuntimeException("Card not found or inactive"));

        if (card.getCurrency() != Currency.NGN) {
            throw new RuntimeException("Only NGN cards can be used for top-up");
        }

        if (card.getUser().getUserId().equals(receiver.getUserId())) {
            throw new RuntimeException("You cannot top-up using your own card");
        }

        if (!card.getCvv().equals(req.cvv())) throw new RuntimeException("Invalid CVV");
        if (!card.getExpiryDate().equals(req.expiry())) throw new RuntimeException("Invalid expiry date");
        if (!card.getPin().equals(req.pin())) throw new RuntimeException("Incorrect PIN");

        Wallet senderWallet = walletService.getWalletByUserAndCurrency(card.getUser(), Currency.NGN);
        if (senderWallet.getBalance() < req.amount()) {
            throw new RuntimeException("Insufficient balance. Sender needs at least ₦" + req.amount());
        }

        Wallet receiverWallet = walletService.getWalletByCurrency(token, Currency.NGN);


        senderWallet.setBalance(senderWallet.getBalance() - req.amount());
        receiverWallet.setBalance(receiverWallet.getBalance() + req.amount());

        walletService.saveWallet(senderWallet);
        walletService.saveWallet(receiverWallet);

        return new TopUpResponse(
                true,
                "Top-up successful! ₦" + String.format("%,.2f", req.amount()) + " received",
                req.amount()
        );
    }

    private String generateMilesBankCard() {
        StringBuilder sb = new StringBuilder(MILES_BANK_BIN);
        for (int i = 6; i < 15; i++) sb.append(random.nextInt(10));
        sb.append(calculateLuhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            if (alternate) { digit *= 2; if (digit > 9) digit -= 9; }
            sum += digit;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    private String generateExpiry() {
        return YearMonth.now().plusYears(4).format(DateTimeFormatter.ofPattern("MM/yy"));
    }

    private String generateCVV() {
        return String.format("%03d", random.nextInt(1000));
    }

    public Card getNgnCard(String token) {
        return cardRepository.findByUserAndCurrencyAndIsActiveTrue(
                userService.getAuthenticatedUser(token), Currency.NGN).orElse(null);
    }

    public Card getUsdCard(String token) {
        return cardRepository.findByUserAndCurrencyAndIsActiveTrue(
                userService.getAuthenticatedUser(token), Currency.USD).orElse(null);
    }
}