package com.example.opaybanking.service;

import com.example.opaybanking.dto.NameEnquiryResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;



@Service
public class NameEnquiryService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    public NameEnquiryResponse verifyAccount(String accountNumber, String bankCode) {
        try {
            String url = "https://api.paystack.co/bank/resolve?account_number=" + accountNumber + "&bank_code=" + bankCode;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + paystackSecretKey);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, entity, String.class);

            JSONObject body = new JSONObject(resp.getBody());
            boolean status = body.optBoolean("status", false);

            if (!status) {
                String msg = body.optString("message", "Failed to resolve account");
                return NameEnquiryResponse.failed(msg, accountNumber, bankCode);
            }

            JSONObject data = body.getJSONObject("data");
            String accName = data.optString("account_name", "Unknown");
            String accNum = data.optString("account_number", accountNumber);

            return NameEnquiryResponse.success(accName, accNum, bankCode);

        } catch (HttpClientErrorException.TooManyRequests ex) {
            // Handle rate limit specifically
            return NameEnquiryResponse.failed(
                    "You have exceeded the daily limit of 3 live bank account verifications in Paystack test mode. " +
                            "Please try again tomorrow or upgrade to live mode for unlimited verifications.",
                    accountNumber,
                    bankCode
            );
        } catch (HttpClientErrorException ex) {
            try {
                JSONObject errorBody = new JSONObject(ex.getResponseBodyAsString());
                String message = errorBody.optString("message", ex.getStatusText());

                if (message.contains("limit") || message.contains("exceeded")) {
                    return NameEnquiryResponse.failed(
                            "Daily verification limit reached. You can only verify 3 accounts per day in test mode. " +
                                    "Please try again tomorrow.",
                            accountNumber,
                            bankCode
                    );
                }

                return NameEnquiryResponse.failed(message, accountNumber, bankCode);
            } catch (Exception jsonEx) {
                return NameEnquiryResponse.failed(ex.getStatusText(), accountNumber, bankCode);
            }
        } catch (Exception ex) {
            return NameEnquiryResponse.failed("Network error: " + ex.getMessage(), accountNumber, bankCode);
        }
    }
}