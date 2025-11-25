package com.example.opaybanking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "SKIP_EMAIL",
        havingValue = "true",
        matchIfMissing = true
)
public class EmailService {

    private final JavaMailSender mailSender;
    private final OtpService otpService;

    @Autowired
    public EmailService(JavaMailSender mailSender, OtpService otpService) {
        this.mailSender = mailSender;
        this.otpService = otpService;
    }

    public String getWelcomeEmailHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <title>Welcome to Miles Bank</title>
                <style>
                    body { margin: 0; padding: 0; font-family: 'Lato', Arial, sans-serif; background-color: #f4f7fa; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
                    .header { background-color: #1e90ff; color: #fff; text-align: center; padding: 30px; }
                    .header h1 { margin: 0; font-size: 32px; font-weight: 700; }
                    .content { padding: 40px 30px; text-align: center; }
                    .content h2 { color: #1e90ff; font-size: 26px; margin-bottom: 15px; font-weight: 400; }
                    .content p { font-size: 16px; line-height: 1.6; color: #555; margin-bottom: 20px; }
                    .btn { display: inline-block; padding: 12px 25px; background-color: #1e90ff; color: #fff; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: 700; transition: background-color 0.3s ease; }
                    .btn:hover { background-color: #1678d7; }
                    .personal-info { margin-top: 25px; font-size: 14px; color: #777; }
                    .personal-info a { color: #1e90ff; text-decoration: none; margin: 0 10px; font-weight: 600; }
                    .personal-info a:hover { text-decoration: underline; }
                    .footer { background-color: #f1f1f1; padding: 20px; text-align: center; font-size: 12px; color: #888; }
                    .footer a { color: #1e90ff; text-decoration: none; }
                    .footer a:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Miles Bank</h1>
                    </div>
                    <div class="content">
                        <h2>Welcome, {firstName}!</h2>
                        <p>Thank you for joining Miles Bank! We’re thrilled to have you as part of our community. A verification code will be sent shortly to complete your registration.</p>
                        <div class="personal-info">
                            <p>Developed by: Oyedokun Lukman</p>
                            <p><a href="https://github.com/lukmanOye" target="_blank">GitHub</a> | <a href="mailto:oyedokun.lukmanoye@gmail.com">Email</a> | <a href="tel:+23407049642241">Phone: +23407049642241</a> | <a href="https://linkedin.com/in/oyedokun-lukman" target="_blank">LinkedIn</a> | <a href="https://mywebsite.com" target="_blank">Website</a></p>
                        </div>
                        <a href="#" class="btn">Get Started</a>
                    </div>
                    <div class="footer">
                        <p>© 2025 Miles Bank. All rights reserved. <a href="#">Unsubscribe</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.replace("{firstName}", firstName);
    }

    public String getOtpEmailHtml(String otp) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <title>Verify Your Miles Bank Account</title>
                <style>
                    body { margin: 0; padding: 0; font-family: 'Lato', Arial, sans-serif; background-color: #f4f7fa; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
                    .header { background-color: #1e90ff; color: #fff; text-align: center; padding: 30px; }
                    .header h1 { margin: 0; font-size: 32px; font-weight: 700; }
                    .content { padding: 40px 30px; text-align: center; }
                    .content h2 { color: #1e90ff; font-size: 26px; margin-bottom: 15px; font-weight: 400; }
                    .content p { font-size: 16px; line-height: 1.6; color: #555; margin-bottom: 20px; }
                    .otp-box { background-color: #1e90ff; padding: 20px; border-radius: 8px; display: inline-block; font-size: 24px; font-weight: 700; color: #fff; margin: 20px 0; }
                    .btn { display: inline-block; padding: 12px 25px; background-color: #1e90ff; color: #fff; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: 700; transition: background-color 0.3s ease; }
                    .btn:hover { background-color: #1678d7; }
                    .personal-info { margin-top: 25px; font-size: 14px; color: #777; }
                    .personal-info a { color: #1e90ff; text-decoration: none; margin: 0 10px; font-weight: 600; }
                    .personal-info a:hover { text-decoration: underline; }
                    .footer { background-color: #f1f1f1; padding: 20px; text-align: center; font-size: 12px; color: #888; }
                    .footer a { color: #1e90ff; text-decoration: none; }
                    .footer a:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Miles Bank</h1>
                    </div>
                    <div class="content">
                        <h2>Verify Your Login</h2>
                        <p>Please use the code below to verify your account. This code expires in 10 minutes.</p>
                        <div class="otp-box">{otp}</div>
                        <p>Do not share this code with anyone.</p>
                        <div class="personal-info">
                            <p>Developed by: Oyedokun Lukman</p>
                            <p><a href="https://github.com/lukmanOye" target="_blank">GitHub</a> | <a href="mailto:oyedokun.lukmanoye@gmail.com">Email</a> | <a href="tel:+23407049642241">Phone: +23407049642241</a> | <a href="https://linkedin.com/in/oyedokun-lukman" target="_blank">LinkedIn</a> | <a href="https://mywebsite.com" target="_blank">Website</a></p>
                        </div>
                        <a href="#" class="btn">Contact Support</a>
                    </div>
                    <div class="footer">
                        <p>© 2025 Miles Bank. All rights reserved. <a href="#">Unsubscribe</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.replace("{otp}", otp);
    }

    public String getUpdateNotificationHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <title>Account Update Notification</title>
                <style>
                    body { margin: 0; padding: 0; font-family: 'Lato', Arial, sans-serif; background-color: #f4f7fa; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
                    .header { background-color: #1e90ff; color: #fff; text-align: center; padding: 30px; }
                    .header h1 { margin: 0; font-size: 32px; font-weight: 700; }
                    .content { padding: 40px 30px; text-align: center; }
                    .content h2 { color: #1e90ff; font-size: 26px; margin-bottom: 15px; font-weight: 400; }
                    .content p { font-size: 16px; line-height: 1.6; color: #555; margin-bottom: 20px; }
                    .btn { display: inline-block; padding: 12px 25px; background-color: #1e90ff; color: #fff; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: 700; transition: background-color 0.3s ease; }
                    .btn:hover { background-color: #1678d7; }
                    .personal-info { margin-top: 25px; font-size: 14px; color: #777; }
                    .personal-info a { color: #1e90ff; text-decoration: none; margin: 0 10px; font-weight: 600; }
                    .personal-info a:hover { text-decoration: underline; }
                    .footer { background-color: #f1f1f1; padding: 20px; text-align: center; font-size: 12px; color: #888; }
                    .footer a { color: #1e90ff; text-decoration: none; }
                    .footer a:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Miles Bank</h1>
                    </div>
                    <div class="content">
                        <h2>Account Update Notification</h2>
                        <p>Dear {firstName}, your account details have been successfully updated.</p>
                        <p>If this was not you, please contact support immediately.</p>
                        <div class="personal-info">
                            <p>Developed by: Oyedokun Lukman</p>
                            <p><a href="https://github.com/lukmanOye" target="_blank">GitHub</a> | <a href="mailto:oyedokun.lukmanoye@gmail.com">Email</a> | <a href="tel:+23407049642241">Phone: +23407049642241</a> | <a href="https://linkedin.com/in/oyedokun-lukman" target="_blank">LinkedIn</a> | <a href="https://mywebsite.com" target="_blank">Website</a></p>
                        </div>
                        <a href="#" class="btn">Contact Support</a>
                    </div>
                    <div class="footer">
                        <p>© 2025 Miles Bank. All rights reserved. <a href="#">Unsubscribe</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.replace("{firstName}", firstName);
    }

    @Async
    public void sendWelcomeEmail(String to, String firstName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom("Miles Bank <milesbrain280@gmail.com>");
        helper.setReplyTo(to);
        helper.setSubject("Welcome to Miles Bank");
        helper.setText(getWelcomeEmailHtml(firstName), true);
        mailSender.send(message);
    }

    @Async
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        String cachedOtp = otpService.getOtp(to);
        if (cachedOtp != null && !cachedOtp.equals(otp)) {
            System.out.println("OTP mismatch: Sent " + otp + " vs Cached " + cachedOtp);
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom("Miles Bank <milesbrain280@gmail.com>");
        helper.setReplyTo(to);
        helper.setSubject("Verify Your Login");
        helper.setText(getOtpEmailHtml(otp), true);
        mailSender.send(message);
    }

    @Async
    public void sendUpdateNotification(String to, String firstName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom("Miles Bank <milesbrain280@gmail.com>");
        helper.setReplyTo(to);
        helper.setSubject("Your Account Has Been Updated");
        helper.setText(getUpdateNotificationHtml(firstName), true);
        mailSender.send(message);
    }
}