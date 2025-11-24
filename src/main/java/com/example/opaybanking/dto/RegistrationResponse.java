package com.example.opaybanking.dto;

public record RegistrationResponse (
    String message,
    String loginUrl,
    String otp,
    String email){

}