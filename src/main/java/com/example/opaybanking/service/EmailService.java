package com.example.opaybanking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final OtpService otpService;
    private final boolean skipEmail;

    public EmailService(JavaMailSender mailSender, OtpService otpService,
                        @Value("${SKIP_EMAIL:false}") boolean skipEmail) {
        this.mailSender = mailSender;
        this.otpService = otpService;
        this.skipEmail = skipEmail;
    }

    private void logSkipped(String action) {
        if (skipEmail) {
            System.out.println("SKIP_EMAIL=true → " + action + " (Email sending skipped - Development Mode)");
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String firstName) throws MessagingException {
        logSkipped("Welcome email to " + to);
        if (skipEmail) return;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom("Miles Bank <milesbrain280@gmail.com>");
        helper.setSubject("Welcome to Miles Bank");
        helper.setText(getWelcomeEmailHtml(firstName), true);
        mailSender.send(message);
    }

    @Async
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        if (skipEmail) {
            System.out.println("OTP FOR " + to + " → " + otp + " (Email skipped - SKIP_EMAIL=true)");
            return;
        }

        String cachedOtp = otpService.getOtp(to);
        if (cachedOtp != null && !cachedOtp.equals(otp)) {
            System.out.println("OTP mismatch: Sent " + otp + " vs Cached " + cachedOtp);
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom("Miles Bank <milesbrain280@gmail.com>");
        helper.setSubject("Verify Your Login - Miles Bank");
        helper.setText(getOtpEmailHtml(otp), true);
        mailSender.send(message);
    }

    @Async
    public void sendUpdateNotification(String to, String firstName) throws MessagingException {
        logSkipped("Update notification to " + to);
        if (skipEmail) return;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom("Miles Bank <milesbrain280@gmail.com>");
        helper.setSubject("Your Account Has Been Updated");
        helper.setText(getUpdateNotificationHtml(firstName), true);
        mailSender.send(message);
    }

    // BEAUTIFUL HTML TEMPLATES BELOW

    public String getWelcomeEmailHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Welcome to Miles Bank</title>
                <style>
                    body { margin: 0; padding: 0; font-family: 'Lato', Arial, sans-serif; background-color: #f4f7fa; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                    .header { background-color: #1e90ff; color: #fff; text-align: center; padding: 30px; }
                    .header h1 { margin: 0; font-size: 32px; font-weight: 700; }
                    .content { padding: 40px 30px; text-align: center; }
                    .content h2 { color: #1e90ff; font-size: 26px; margin-bottom: 15px; }
                    .content p { font-size: 16px; line-height: 1.6; color: #555; margin-bottom: 20px; }
                    .btn { display: inline-block; padding: 14px 28px; background-color: #1e90ff; color: #fff; text-decoration: none; border-radius: 6px; font-weight: 700; }
                    .footer { background-color: #f1f1f1; padding: 20px; text-align: center; font-size: 12px; color: #888; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>Miles Bank</h1></div>
                    <div class="content">
                        <h2>Welcome aboard, %s!</h2>
                        <p>Your Miles Bank account is ready. Enjoy seamless banking, instant transfers, and zero fees.</p>
                        <a href="#" class="btn">Explore Your Account</a>
                    </div>
                    <div class="footer">
                        <p>© 2025 Miles Bank • Built with ❤️ by Oyedokun Lukman</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName);
    }

    public String getOtpEmailHtml(String otp) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Your Verification Code</title>
                <style>
                    body { margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f7fa; }
                    .container { max-width: 600px; margin: 40px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #1e90ff, #00bfff); padding: 40px 20px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 36px; font-weight: bold; }
                    .content { padding: 40px; text-align: center; }
                    .otp { font-size: 42px; font-weight: bold; letter-spacing: 10px; background: #1e90ff; color: white; padding: 20px; border-radius: 12px; display: inline-block; margin: 20px 0; }
                    .warning { color: #e74c3c; font-weight: bold; margin: 20px 0; }
                    .footer { background: #f8f9fa; padding: 20px; font-size: 12px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>Miles Bank</h1></div>
                    <div class="content">
                        <h2>Your Verification Code</h2>
                        <div class="otp">%s</div>
                        <p>This code expires in <strong>10 minutes</strong>.</p>
                        <p class="warning">Never share this code with anyone — even Miles Bank staff.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Miles Bank • Secure • Fast • Yours</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(otp);
    }

    public String getUpdateNotificationHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Account Update Confirmed</title>
                <style>
                    body { font-family: Arial, sans-serif; background: #f4f7fa; padding: 20px; }
                    .card { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; }
                    .header { background: #1e90ff; color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; text-align: center; }
                    .success { font-size: 50px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header"><h1>Miles Bank</h1></div>
                    <div class="content">
                        <div class="success">Checkmark</div>
                        <h2>Account Updated Successfully</h2>
                        <p>Hi %s, your account details have been updated.</p>
                        <p>If you didn't make this change, contact support immediately.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName);
    }
}