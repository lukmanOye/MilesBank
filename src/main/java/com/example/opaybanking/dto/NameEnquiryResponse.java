package com.example.opaybanking.dto;

public class NameEnquiryResponse {
    private boolean success;
    private String accountName;
    private String accountNumber;
    private String bankCode;
    private String message;

    public NameEnquiryResponse() {}

    public NameEnquiryResponse(boolean success, String accountName, String accountNumber,
                               String bankCode, String message) {
        this.success = success;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.message = message;
    }

    public static NameEnquiryResponse success(String accountName, String accountNumber, String bankCode) {
        return new NameEnquiryResponse(true, accountName, accountNumber, bankCode, "OK");
    }

    public static NameEnquiryResponse failed(String message, String accountNumber, String bankCode) {
        return new NameEnquiryResponse(false, null, accountNumber, bankCode, message);
    }

    public static NameEnquiryResponse failed(String message) {
        return new NameEnquiryResponse(false, null, null, "190909", message);
    }

    public static NameEnquiryResponse failed(String message, String bankCode) {
        return new NameEnquiryResponse(false, null, null, bankCode, message);
    }

    public boolean isSuccess() { return success; }
    public String getAccountName() { return accountName; }
    public String getAccountNumber() { return accountNumber; }
    public String getBankCode() { return bankCode; }
    public String getMessage() { return message; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public void setMessage(String message) { this.message = message; }
}
