    package com.example.opaybanking.service;

    import com.example.opaybanking.dto.*;
    import com.example.opaybanking.enums.*;
    import com.example.opaybanking.model.*;
    import com.example.opaybanking.repo.*;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.time.Month;
    import java.time.YearMonth;
    import java.util.List;
    import java.util.UUID;

    @Service

    public class TransactionService {

        private final TransactionRepo transactionRepo;
        private final WalletRepo walletRepo;
        private final WalletService walletService;
        private final NameEnquiryService nameEnquiryService;
        private final userService userService;
        private final ExchangeRateService exchangeRateService;
        private final BankRepo bankRepo;
        private final BankService bankService;

        private static final String MILES_BANK = "Miles Bank";
        private static final String MILES_BANK_CODE = "190909";

        public TransactionService(TransactionRepo transactionRepo, WalletRepo walletRepo, WalletService walletService,
                                  NameEnquiryService nameEnquiryService, com.example.opaybanking.service.userService userService, ExchangeRateService exchangeRateService, BankRepo bankRepo, BankService bankService) {
            this.transactionRepo = transactionRepo;
            this.walletRepo = walletRepo;
            this.walletService = walletService;
            this.nameEnquiryService = nameEnquiryService;
            this.userService = userService;
            this.exchangeRateService = exchangeRateService;
            this.bankRepo = bankRepo;
            this.bankService = bankService;
        }

        private Wallet getUserWalletByCurrency(String token, String currencyStr) {
            Currency currency;
            try {
                currency = Currency.valueOf(currencyStr.toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Invalid currency. Use NGN or USD");
            }

            User user = userService.getAuthenticatedUser(token);
            return walletRepo.findFirstByUserAndCurrency(user, currency)
                    .orElseThrow(() -> new RuntimeException(
                            "You don't have a " + currency + " wallet. Create one first."
                    ));
        }

        @Transactional
        public TransferResponse internalTransfer(InternalTransferRequest req, String token) {
            Wallet fromWallet = getUserWalletByCurrency(token, req.currency());

            if (fromWallet.getAccountNumber().equals(req.toAccountNumber())) {
                throw new RuntimeException("You cannot transfer to your own account");
            }
            if (!walletService.verifyPin(fromWallet.getWalletId().longValue(), req.pin(), token)) {
                throw new RuntimeException("Invalid PIN");
            }
            if (fromWallet.getBalance() < req.amount()) {
                throw new RuntimeException("Insufficient balance in " + req.currency() + " wallet");
            }

            NameEnquiryResponse enquiry = walletService.verifyMilesBankAccount(req.toAccountNumber());
            if (!enquiry.isSuccess()) throw new RuntimeException("Recipient not found");

            Wallet toWallet = walletRepo.findByAccountNumber(req.toAccountNumber())
                    .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));

            if (fromWallet.getCurrency() != toWallet.getCurrency()) {
                throw new RuntimeException("Use cross-currency transfer for different currencies");
            }

            fromWallet.setBalance(fromWallet.getBalance() - req.amount());
            toWallet.setBalance(toWallet.getBalance() + req.amount());
            walletRepo.save(fromWallet);
            walletRepo.save(toWallet);

            String ref = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            String senderDesc = (req.description() != null && !req.description().trim().isEmpty())
                    ? req.description().trim()
                    : "Transfer to " + enquiry.getAccountName();

            String receiverDesc = (req.description() != null && !req.description().trim().isEmpty())
                    ? req.description().trim()
                    : "From " + fromWallet.getAccountName();

            Transaction outTx = createTx(
                    userService.getAuthenticatedUser(token),
                    fromWallet,
                    req.amount(),
                    TransactionType.TRANSFER_OUT,
                    ref,
                    senderDesc,
                    req.toAccountNumber(),
                    enquiry.getAccountName(),
                    MILES_BANK
            );

            Transaction inTx = createTx(
                    toWallet.getUser(),
                    toWallet,
                    req.amount(),
                    TransactionType.TRANSFER_IN,
                    ref,
                    receiverDesc,
                    fromWallet.getAccountNumber(),
                    fromWallet.getAccountName(),
                    MILES_BANK
            );

            transactionRepo.save(outTx);
            transactionRepo.save(inTx);

            return new TransferResponse(
                    true,
                    "Transfer successful",
                    ref,
                    fromWallet.getAccountNumber(),
                    req.toAccountNumber(),
                    enquiry.getAccountName(),
                    MILES_BANK,
                    req.amount(),
                    fromWallet.getBalance()
            );
        }

        @Transactional
        public TransferResponse crossCurrencyTransfer(CrossCurrencyTransferRequest req, String token) {
            Wallet fromWallet = getUserWalletByCurrency(token, req.currency());

            if (fromWallet.getAccountNumber().equals(req.toAccountNumber())) {
                throw new RuntimeException("You cannot transfer to your own account");
            }
            if (!walletService.verifyPin(fromWallet.getWalletId().longValue(), req.pin(), token)) {
                throw new RuntimeException("Invalid PIN");
            }
            if (fromWallet.getBalance() < req.amount()) {
                throw new RuntimeException("Insufficient balance in " + req.currency() + " wallet");
            }

            NameEnquiryResponse enquiry = walletService.verifyMilesBankAccount(req.toAccountNumber());
            if (!enquiry.isSuccess()) throw new RuntimeException("Recipient not found");

            Wallet toWallet = walletRepo.findByAccountNumber(req.toAccountNumber())
                    .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));

            if (fromWallet.getCurrency() == toWallet.getCurrency()) {
                throw new RuntimeException("Use internal transfer for same currency");
            }

            ExchangeRate rate = exchangeRateService.getRate();
            double converted = fromWallet.getCurrency() == Currency.NGN
                    ? req.amount() / rate.getNgnToUsd()
                    : req.amount() * rate.getNgnToUsd();

            fromWallet.setBalance(fromWallet.getBalance() - req.amount());
            toWallet.setBalance(toWallet.getBalance() + converted);
            walletRepo.save(fromWallet);
            walletRepo.save(toWallet);

            String ref = "FX" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Transaction outTx = createTx(userService.getAuthenticatedUser(token), fromWallet, req.amount(),
                    TransactionType.CURRENCY_EXCHANGE_OUT, ref, "FX Transfer", req.toAccountNumber(), enquiry.getAccountName(), MILES_BANK);
            Transaction inTx = createTx(toWallet.getUser(), toWallet, converted,
                    TransactionType.CURRENCY_EXCHANGE_IN, ref, "FX Received", fromWallet.getAccountNumber(), fromWallet.getAccountName(), MILES_BANK);

            transactionRepo.save(outTx);
            transactionRepo.save(inTx);

            return new TransferResponse(true, "FX Transfer successful", ref,
                    fromWallet.getAccountNumber(), req.toAccountNumber(), enquiry.getAccountName(),
                    MILES_BANK, converted, toWallet.getBalance());
        }

        @Transactional
        public TransferResponse externalTransfer(ExternalTransferRequest req, String token) {
            Wallet fromWallet = getUserWalletByCurrency(token, req.currency());

            if (!walletService.verifyPin(fromWallet.getWalletId().longValue(), req.pin(), token))
                throw new RuntimeException("Invalid PIN");
            if (fromWallet.getBalance() < req.amount())
                throw new RuntimeException("Insufficient balance");

            if (MILES_BANK_CODE.equals(req.bankCode())) {
                return processMilesBankTransfer(req, fromWallet, token);
            }


            String bankName = bankService.resolveBankName(req.bankCode());
            if ("Unknown Bank".equals(bankName))
                throw new RuntimeException("Bank not supported. Code: " + req.bankCode());

            NameEnquiryResponse enquiry = nameEnquiryService.verifyAccount(req.accountNumber(), req.bankCode());
            if (!enquiry.isSuccess())
                throw new RuntimeException("Name enquiry failed: " + enquiry.getMessage());



            fromWallet.setBalance(fromWallet.getBalance() - req.amount());
            walletRepo.save(fromWallet);

            String ref = "EXT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Transaction tx = createTx(
                    userService.getAuthenticatedUser(token),
                    fromWallet,
                    req.amount(),
                    TransactionType.TRANSFER_OUT,
                    ref,
                    req.description() != null && !req.description().isBlank() ? req.description() : "Transfer to " + req.accountName(),
                    req.accountNumber(),
                    req.accountName(),
                    bankName
            );
            transactionRepo.save(tx);

            return new TransferResponse(
                    true,
                    "Transfer scheduled successfully",
                    ref,
                    fromWallet.getAccountNumber(),
                    req.accountNumber(),
                    req.accountName(),
                    bankName,
                    req.amount(),
                    fromWallet.getBalance()
            );
        }
        private TransferResponse processMilesBankTransfer(ExternalTransferRequest req, Wallet fromWallet, String token) {
            User sender = userService.getAuthenticatedUser(token);

            NameEnquiryResponse enquiry = walletService.verifyMilesBankAccount(req.accountNumber());
            if (!enquiry.isSuccess()) {
                throw new RuntimeException("Recipient not found in Miles Bank");
            }

            Wallet toWallet = walletRepo.findByAccountNumber(req.accountNumber())
                    .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));

            if (fromWallet.getAccountNumber().equals(toWallet.getAccountNumber())) {
                throw new RuntimeException("You cannot transfer to your own account");
            }

            fromWallet.setBalance(fromWallet.getBalance() - req.amount());
            toWallet.setBalance(toWallet.getBalance() + req.amount());
            walletRepo.save(fromWallet);
            walletRepo.save(toWallet);

            String ref = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Transaction outTx = createTx(
                    sender, fromWallet, req.amount(), TransactionType.TRANSFER_OUT,
                    ref, "Transfer to " + enquiry.getAccountName(),
                    req.accountNumber(), enquiry.getAccountName(), MILES_BANK
            );

            Transaction inTx = createTx(
                    toWallet.getUser(), toWallet, req.amount(), TransactionType.TRANSFER_IN,
                    ref, "From " + fromWallet.getAccountName(),
                    fromWallet.getAccountNumber(), fromWallet.getAccountName(), MILES_BANK
            );

            transactionRepo.save(outTx);
            transactionRepo.save(inTx);

            return new TransferResponse(true, "Transfer successful", ref,
                    fromWallet.getAccountNumber(), req.accountNumber(),
                    enquiry.getAccountName(), "Miles Bank", req.amount(), fromWallet.getBalance());
        }

        private Transaction createTx(User user, Wallet wallet, Double amount, TransactionType type,
                                     String ref, String desc, String benAcc, String benName, String benBank) {
            Transaction tx = new Transaction();
            tx.setUser(user);
            tx.setWallet(wallet);
            tx.setAmount(amount);
            tx.setTransactionType(type);
            tx.setReference(ref);
            tx.setTransactionStatus(Status.SUCCESSFUL);
            tx.setDescription(desc);
            tx.setBeneficiaryAccount(benAcc);
            tx.setBeneficiaryName(benName);
            tx.setBeneficiaryBank(benBank);
            tx.setCreatedAt(LocalDateTime.now());
            return tx;
        }

        public List<TransactionResponseDto> getUserTransactionsByDateRange(String token, LocalDateTime start, LocalDateTime end) {
            User user = userService.getAuthenticatedUser(token);
            Long userId = user.getUserId().longValue();

            if (start != null && end != null) {
                List<Transaction> txns = transactionRepo.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end);
                return txns.stream().map(this::toDto).toList();
            } else {
                List<Transaction> txns = transactionRepo.findByUserAndDateRangeOrderByDateDesc(userId, start, end);
                return txns.stream().map(this::toDto).toList();
            }
        }

        public List<TransactionResponseDto> getUserTransactionsByAmountDesc(String token) {
            User user = userService.getAuthenticatedUser(token);
            List<Transaction> txns = transactionRepo.findByUserUserIdOrderByAmountDesc(user.getUserId().longValue());
            return txns.stream().map(this::toDto).toList();
        }

        public List<TransactionResponseDto> getTransactionsByMonth(String token, String monthName) {
            User user = userService.getAuthenticatedUser(token);
            Long userId = user.getUserId().longValue();

            Month month;
            try {
                month = Month.valueOf(monthName.toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Invalid month. Use: JANUARY, FEBRUARY, MARCH, etc.");
            }

            int year = LocalDateTime.now().getYear();
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Transaction> txns = transactionRepo.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end);
            return txns.stream().map(this::toDto).toList();
        }

        public Transaction getTransaction(Long transactionId, String token) {
            User user = userService.getAuthenticatedUser(token);
            Transaction tx = transactionRepo.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            if (!tx.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
                throw new RuntimeException("Access denied");
            }
            return tx;
        }


        public List<Transaction> getWalletTransactions(Long walletId, String token) {
            walletService.getWalletById(walletId, token);
            return transactionRepo.findByWalletWalletIdOrderByCreatedAtDesc(walletId);
        }

        private TransactionResponseDto toDto(Transaction tx) {
            String formattedDate = tx.getCreatedAt()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

            return new TransactionResponseDto(
                    (long) tx.getTransactionId(),
                    tx.getTransactionType().name(),
                    tx.getReference(),
                    tx.getAmount(),
                    tx.getBeneficiaryName(),
                    tx.getBeneficiaryAccount(),
                    tx.getBeneficiaryBank(),
                    tx.getDescription() != null ? tx.getDescription() : getDefaultDescription(tx),
                    tx.getTransactionStatus().name(),
                    formattedDate
            );
        }

        private String getDefaultDescription(Transaction tx) {
            return switch (tx.getTransactionType()) {
                case TRANSFER_OUT -> "Sent to " + tx.getBeneficiaryName();
                case TRANSFER_IN -> "Received from " + tx.getWallet().getAccountName();
                case CURRENCY_EXCHANGE_OUT -> "FX Transfer Out";
                case CURRENCY_EXCHANGE_IN -> "FX Transfer In";
                default -> tx.getTransactionType().name();
            };
        }

        public List<Transaction> getAllTransactions(String token) {
            User user = userService.getAuthenticatedUser(token);
            if (user.getRole() != Role.ADMIN) {
                throw new RuntimeException("Admin access required");
            }
            return transactionRepo.findAllByOrderByCreatedAtDesc();
        }


        public List<TransactionResponseDto> getUserTransactions(String token) {
            User user = userService.getAuthenticatedUser(token);
            List<Transaction> txns = transactionRepo.findByUserUserIdOrderByCreatedAtDesc(user.getUserId().longValue());
            return txns.stream().map(this::toDto).toList();
        }
    }
