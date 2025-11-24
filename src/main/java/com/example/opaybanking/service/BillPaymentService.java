package com.example.opaybanking.service;

import com.example.opaybanking.config.*;
import com.example.opaybanking.dto.BillPaymentRequest;
import com.example.opaybanking.dto.BillPaymentResponse;
import com.example.opaybanking.enums.BillType;
import com.example.opaybanking.enums.Currency;
import com.example.opaybanking.enums.Status;
import com.example.opaybanking.model.*;
import com.example.opaybanking.repo.billPaymentRepo;
import com.example.opaybanking.util.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BillPaymentService {

    private final billPaymentRepo billPaymentRepo;
    private final WalletService walletService;
    private final userService userService;
    private final PhoneNumberValidator phoneNumberValidator;

    public BillPaymentService(com.example.opaybanking.repo.billPaymentRepo billPaymentRepo, WalletService walletService, userService userService, PhoneNumberValidator phoneNumberValidator) {
        this.billPaymentRepo = billPaymentRepo;
        this.walletService = walletService;
        this.userService = userService;
        this.phoneNumberValidator = phoneNumberValidator;
    }

    @Transactional
    public BillPaymentResponse processBillPayment(BillPaymentRequest req, String token) {
        User user = userService.getAuthenticatedUser(token);

        Wallet ngnWallet = walletService.getWalletByCurrency(token, Currency.NGN);
        double amountToPay = getAmountToPay(req);

        if (ngnWallet.getBalance() < amountToPay) {
            return BillPaymentResponse.error("Insufficient NGN balance. Required: ₦" + String.format("%,.2f", amountToPay));
        }

        if (req.billType() == BillType.AIRTIME || req.billType() == BillType.DATA) {
            if (req.phoneNumber() == null || !phoneNumberValidator.isValidNigerianNumber(req.phoneNumber())) {
                return BillPaymentResponse.error("Invalid Nigerian phone number");
            }
        }

        return switch (req.billType()) {
            case AIRTIME -> processAirtime(req, user, ngnWallet, amountToPay);
            case DATA -> processData(req, user, ngnWallet);
            case ELECTRICITY -> processElectricity(req, user, ngnWallet, amountToPay);
            case TV -> processTv(req, user, ngnWallet);
        };
    }

    private double getAmountToPay(BillPaymentRequest req) {
        return switch (req.billType()) {
            case DATA -> findDataPlan(req.network(), req.planId()).getPrice();
            case TV -> findTvPlan(req.tvProvider(), req.planId()).getPrice();
            case AIRTIME, ELECTRICITY -> req.amount();
        };
    }

    private BillPaymentResponse processAirtime(BillPaymentRequest req, User user, Wallet wallet, double amount) {
        String network = getNetwork(req.network(), req.phoneNumber());
        String ref = generateRef("AIR");
        String details = String.format("₦%,.0f airtime → %s (%s)", amount, req.phoneNumber(), network);

        deductFromWallet(wallet, amount);
        saveBillPayment(user, BillType.AIRTIME, amount, ref, details, req.phoneNumber(), network, null);

        return BillPaymentResponse.success("Airtime purchased successfully", ref, amount, details);
    }

    private BillPaymentResponse processData(BillPaymentRequest req, User user, Wallet wallet) {
        String network = getNetwork(req.network(), req.phoneNumber());
        DataPlansConfig.DataPlan plan = findDataPlan(network, req.planId());
        String ref = generateRef("DAT");
        String details = plan.getName() + " data → " + req.phoneNumber();

        deductFromWallet(wallet, plan.getPrice());
        saveBillPayment(user, BillType.DATA, plan.getPrice(), ref, details, req.phoneNumber(), network, req.planId());

        return BillPaymentResponse.success("Data purchased successfully", ref, plan.getPrice(), details);
    }

    private BillPaymentResponse processElectricity(BillPaymentRequest req, User user, Wallet wallet, double amount) {
        if (req.meterNumber() == null || !req.meterNumber().matches("\\d{10,15}")) {
            return BillPaymentResponse.error("Invalid meter number. Must be 10-15 digits");
        }

        String ref = generateRef("ELC");
        String details = String.format("₦%,.0f electricity → Meter %s", amount, req.meterNumber());

        deductFromWallet(wallet, amount);
        saveBillPayment(user, BillType.ELECTRICITY, amount, ref, details, null, null, req.meterNumber());

        return BillPaymentResponse.success("Electricity payment successful", ref, amount, details);
    }

    private BillPaymentResponse processTv(BillPaymentRequest req, User user, Wallet wallet) {
        TvPlansConfig.TvPlan plan = findTvPlan(req.tvProvider(), req.planId());
        String ref = generateRef("TV");
        String details = plan.getName() + " subscription";

        deductFromWallet(wallet, plan.getPrice());
        saveBillPayment(user, BillType.TV, plan.getPrice(), ref, details, req.phoneNumber(), req.tvProvider(), req.planId());

        return BillPaymentResponse.success("TV subscription successful", ref, plan.getPrice(), details);
    }

    private void deductFromWallet(Wallet wallet, double amount) {
        wallet.setBalance(wallet.getBalance() - amount);
        walletService.saveWallet(wallet);
    }

    private void saveBillPayment(User user, BillType type, double amount, String ref, String details,
                                 String phone, String network, String extra) {
        BillPayment payment = new BillPayment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setBillType(type);
        payment.setReference(ref);
        payment.setTransactionStatus(Status.SUCCESSFUL);
        payment.setPhoneNumber(phone);
        payment.setNetwork(network);
        payment.setPlanId(extra);
        payment.setDetails(details);
        payment.setCreatedAt(LocalDateTime.now());

        billPaymentRepo.save(payment);
    }

    private String generateRef(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String getNetwork(String network, String phone) {
        if (network != null && !network.isBlank()) {
            return network.toUpperCase();
        }
        String detected = phoneNumberValidator.detectNetwork(phone);
        if ("UNKNOWN".equals(detected)) {
            throw new RuntimeException("Cannot detect network. Please specify network (MTN, AIRTEL, GLO, 9MOBILE)");
        }
        return detected;
    }

    private DataPlansConfig.DataPlan findDataPlan(String network, String planId) {
        List<DataPlansConfig.DataPlan> plans = DataPlansConfig.NETWORK_DATA_PLANS.get(network.toUpperCase());
        if (plans == null) throw new RuntimeException("Network not supported: " + network);

        return plans.stream()
                .filter(p -> p.getId().equals(planId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid plan ID: " + planId));
    }

    private TvPlansConfig.TvPlan findTvPlan(String provider, String planId) {
        List<TvPlansConfig.TvPlan> plans = TvPlansConfig.TV_PROVIDER_PLANS.get(provider.toUpperCase());
        if (plans == null) throw new RuntimeException("TV provider not supported: " + provider);

        return plans.stream()
                .filter(p -> p.getId().equals(planId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid plan ID: " + planId));
    }

    public List<DataPlansConfig.DataPlan> getDataPlans(String network) {
        return DataPlansConfig.NETWORK_DATA_PLANS.getOrDefault(network.toUpperCase(), List.of());
    }

    public List<TvPlansConfig.TvPlan> getTvPlans(String provider) {
        return TvPlansConfig.TV_PROVIDER_PLANS.getOrDefault(provider.toUpperCase(), List.of());
    }

    public List<AirtimePlansConfig.AirtimePlan> getAirtimePlans(String network) {
        return AirtimePlansConfig.NETWORK_AIRTIME_PLANS.getOrDefault(network.toUpperCase(), List.of());
    }

    public List<BillPayment> getUserBillHistory(String token) {
        User user = userService.getAuthenticatedUser(token);
        return billPaymentRepo.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    }


    public BillPayment getBillPaymentByReference(String reference, String token) {
        userService.getAuthenticatedUser(token);
        return billPaymentRepo.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Bill payment not found"));
    }
}