package com.example.opaybanking.model;

public class OtpRequest {
    private String email;
    private String otp;


    public OtpRequest() {}


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}