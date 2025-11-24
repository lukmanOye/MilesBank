package com.example.opaybanking.service;

import com.example.opaybanking.dto.*;
import com.example.opaybanking.enums.Currency;
import com.example.opaybanking.enums.Role;
import com.example.opaybanking.model.*;
import com.example.opaybanking.repo.BankRepo;
import com.example.opaybanking.repo.WalletRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class WalletService {

    private final WalletRepo walletRepo;
    private final BankRepo bankRepo;
    private final PasswordEncoder passwordEncoder;
    private final userService userService;

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    private final SecureRandom random = new SecureRandom();
    private static final String MILES_BANK_CODE = "190909";

    public WalletService(WalletRepo walletRepo, BankRepo bankRepo, PasswordEncoder passwordEncoder,
                         userService userService) {
        this.walletRepo = walletRepo;
        this.bankRepo = bankRepo;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }


    public Wallet createWallet(String pin, Currency currency, String token) {
        User user = userService.getAuthenticatedUser(token);
        if (walletRepo.existsByUserAndCurrency(user, currency)) {
            throw new RuntimeException(currency + " wallet already exists");
        }
        validatePin(pin);

        Bank bank = bankRepo.findByBankCode(MILES_BANK_CODE)
                .orElseThrow(() -> new RuntimeException("Miles Bank not configured"));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAccountNumber(generateUniqueAccountNumber());
        wallet.setAccountName(user.getFirstName() + " " + user.getLastName());
        wallet.setPin(passwordEncoder.encode(pin));
        wallet.setBank(bank);
        wallet.setBalance(0.0);
        wallet.setCurrency(currency);
        wallet.setCreatedAt(LocalDateTime.now());

        return walletRepo.save(wallet);
    }

    public Wallet createNairaWallet(String pin, String token) {
        return createWallet(pin, Currency.NGN, token);
    }

    public Wallet createDollarWallet(String pin, String token) {
        return createWallet(pin, Currency.USD, token);
    }

    private String generateUniqueAccountNumber() {
        String acc;
        do {
            acc = String.format("%010d", random.nextInt(1_000_000_000));
        } while (walletRepo.existsByAccountNumber(acc));
        return acc;
    }

    private void validatePin(String pin) {
        if (pin == null || pin.isBlank() || pin.length() != 4 || !pin.matches("\\d{4}")) {
            throw new RuntimeException("PIN must be exactly 4 digits");
        }
    }

    public List<WalletResponse> getUserWallets(String token) {
        User user = userService.getAuthenticatedUser(token);
        return walletRepo.findByUser(user).stream()
                .map(WalletResponse::from)
                .toList();
    }

    public Wallet getNairaWallet(String token) {
        return getWalletByCurrency(token, Currency.NGN);
    }

    public Wallet getDollarWallet(String token) {
        return getWalletByCurrency(token, Currency.USD);
    }



    public WalletResponse changeNgnPin(String token, PinUpdateRequest req) {
        return changePin(token, Currency.NGN, req);
    }

    public WalletResponse changeUsdPin(String token, PinUpdateRequest req) {
        return changePin(token, Currency.USD, req);
    }

    private WalletResponse changePin(String token, Currency currency, PinUpdateRequest req) {
        validatePin(req.newPin());
        if (req.oldPin().equals(req.newPin())) throw new RuntimeException("New PIN cannot be same as old");

        Wallet wallet = getWalletByCurrency(token, currency);
        if (!passwordEncoder.matches(req.oldPin(), wallet.getPin())) {
            throw new RuntimeException("Incorrect current PIN");
        }

        wallet.setPin(passwordEncoder.encode(req.newPin()));
        walletRepo.save(wallet);
        return WalletResponse.from(wallet);
    }

    public Wallet getMyWallet(String token) {
        return getNairaWallet(token);
    }

    public boolean verifyPin(Long walletId, String pin, String token) {
        Wallet wallet = walletRepo.findById(walletId.intValue())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return passwordEncoder.matches(pin, wallet.getPin());
    }

    public Wallet getWalletById(Long id, String token) {
        User user = userService.getAuthenticatedUser(token);
        Wallet wallet = walletRepo.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        if (!wallet.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        return wallet;
    }

    public Wallet getWalletByCurrency(String token, Currency currency) {
        User user = userService.getAuthenticatedUser(token);
        return walletRepo.findFirstByUserAndCurrency(user, currency)
                .orElseThrow(() -> new RuntimeException("No " + currency + " wallet found"));
    }

    public void saveWallet(Wallet wallet) {
        walletRepo.save(wallet);
    }

    public NameEnquiryResponse verifyMilesBankAccount(String accountNumber) {
        Wallet wallet = walletRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found in Miles Bank"));

        String fullName = wallet.getUser().getFirstName() + " " + wallet.getUser().getLastName();
        return NameEnquiryResponse.success(fullName.trim(), accountNumber, "190909");
    }

    public List<WalletResponse> getAllWalletsAdmin(String token) {
        System.out.println("=== ADMIN: GET ALL WALLETS REQUEST ===");
        User user = userService.getAuthenticatedUser(token);

        if (user.getRole() != Role.ADMIN) {
            System.out.println("=== ACCESS DENIED: NOT ADMIN ===");
            System.out.println("User email: " + user.getEmail() + ", Role: " + user.getRole());
            throw new RuntimeException("Admin access required. Your role: " + user.getRole());
        }

        List<WalletResponse> wallets = walletRepo.findAll().stream()
                .map(WalletResponse::from)
                .toList();

        System.out.println("Returning " + wallets.size() + " wallets to admin");
        return wallets;
    }


    @Transactional
    public WalletResponse adminUpdateBalance(Integer walletId, Double balance, String token) {
        System.out.println("=== ADMIN: UPDATE BALANCE REQUEST ===");
        System.out.println("Wallet ID: " + walletId + ", New Balance: " + balance);

        User user = userService.getAuthenticatedUser(token);

        if (user.getRole() != Role.ADMIN) {
            System.out.println("=== ACCESS DENIED: NOT ADMIN ===");
            throw new RuntimeException("Admin access required. Your role: " + user.getRole());
        }

        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with ID: " + walletId));

        wallet.setBalance(balance);
        Wallet saved = walletRepo.save(wallet);

        logger.info("Admin {} updated wallet {} balance to {}", user.getEmail(), walletId, balance);
        System.out.println("Balance updated successfully by admin: " + user.getEmail());

        return WalletResponse.from(saved);
    }

    @Transactional
    public void adminDeleteWallet(Integer walletId, String token) {
        System.out.println("=== ADMIN: DELETE WALLET REQUEST ===");
        System.out.println("Wallet ID: " + walletId);

        User user = userService.getAuthenticatedUser(token);

        if (user.getRole() != Role.ADMIN) {
            System.out.println("=== ACCESS DENIED: NOT ADMIN ===");
            throw new RuntimeException("Admin access required. Your role: " + user.getRole());
        }

        if (!walletRepo.existsById(walletId)) {
            throw new RuntimeException("Wallet not found with ID: " + walletId);
        }

        walletRepo.deleteById(walletId);
        logger.info("Admin {} deleted wallet ID: {}", user.getEmail(), walletId);
        System.out.println("Wallet deleted successfully by admin: " + user.getEmail());
    }

    public Wallet getWalletByUserAndCurrency(User user, Currency currency) {
        return walletRepo.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new RuntimeException(
                        user.getFirstName() + " does not have a " + currency + " wallet"
                ));
    }
}