package com.example.

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("Miles Bank <your-gmail@gmail.com>");
        message.setReplyTo("your-gmail@gmail.com");
        message.setSubject("Welcome to Miles Bank");
        message.setText("Dear " + firstName + ",\n\nThanks for creating a bank account with us at Miles Bank! " +
                "We are excited to have you on board. A verification code will be sent shortly to confirm your email.\n\nBest,\nMiles Bank Team");
        mailSender.send(message);
    }

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("Miles Bank <your-gmail@gmail.com>");
        message.setReplyTo("your-gmail@gmail.com");
        message.setSubject("Your Verification Code");
        message.setText("Dear User,\nYour OTP code is: " + otp + ". It expires in 5 minutes. Do not share this code.\nBest,\nMiles Bank Team");
        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("Miles Bank <your-gmail@gmail.com>");
        message.setReplyTo("your-gmail@gmail.com");
        message.setSubject("Verify Your Email");
        message.setText("Dear User,\nClick the link to verify your email: " + verificationLink + "\nBest,\nMiles Bank Team");
        mailSender.send(message);
    }
}