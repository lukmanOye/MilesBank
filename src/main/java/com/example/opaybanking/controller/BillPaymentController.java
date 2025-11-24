package com.example.opaybanking.controller;

import com.example.opaybanking.config.*;
import com.example.opaybanking.dto.BillPaymentRequest;
import com.example.opaybanking.dto.BillPaymentResponse;
import com.example.opaybanking.enums.BillType;
import com.example.opaybanking.model.BillPayment;
import com.example.opaybanking.service.BillPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bill")
@CrossOrigin(origins = "*")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping("/pay")
    public ResponseEntity<BillPaymentResponse> processPayment(
            @RequestBody BillPaymentRequest request,
            @RequestHeader("Authorization") String token) {

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        BillPaymentResponse response = billPaymentService.processBillPayment(request, token);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/data-plans/{network}")
    public ResponseEntity<List<DataPlansConfig.DataPlan>> getDataPlans(
            @PathVariable String network,
            @RequestHeader("Authorization") String token) {

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<DataPlansConfig.DataPlan> plans = billPaymentService.getDataPlans(network);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/tv-plans/{provider}")
    public ResponseEntity<List<TvPlansConfig.TvPlan>> getTvPlans(
            @PathVariable String provider,
            @RequestHeader("Authorization") String token) {

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<TvPlansConfig.TvPlan> plans = billPaymentService.getTvPlans(provider);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/airtime-plans/{network}")
    public ResponseEntity<List<AirtimePlansConfig.AirtimePlan>> getAirtimePlans(
            @PathVariable String network,
            @RequestHeader("Authorization") String token) {

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<AirtimePlansConfig.AirtimePlan> plans = billPaymentService.getAirtimePlans(network);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/networks")
    public ResponseEntity<Map<String, String>> getNetworks(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, String> networks = Map.of(
                "MTN", "MTN Nigeria",
                "AIRTEL", "Airtel Nigeria",
                "GLO", "Globacom Limited",
                "9MOBILE", "9Mobile"
        );
        return ResponseEntity.ok(networks);
    }

    @GetMapping("/tv-providers")
    public ResponseEntity<Map<String, String>> getTvProviders(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, String> providers = Map.of(
                "DSTV", "DStv",
                "GOTV", "GOtv",
                "STARTIMES", "Startimes"
        );
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/history")
    public ResponseEntity<List<BillPayment>> getBillHistory(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<BillPayment> history = billPaymentService.getUserBillHistory(token);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/transaction/{reference}")
    public ResponseEntity<BillPayment> getTransactionByReference(
            @PathVariable String reference,
            @RequestHeader("Authorization") String token) {

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        BillPayment payment = billPaymentService.getBillPaymentByReference(reference, token);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/types")
    public ResponseEntity<Map<String, String>> getBillTypes(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, String> billTypes = Map.of(
                "AIRTIME", "Airtime Top-up",
                "DATA", "Mobile Data",
                "ELECTRICITY", "Electricity Bill",
                "TV", "TV Subscription"
        );
        return ResponseEntity.ok(billTypes);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "Bill Payment Service is running"));
    }
}