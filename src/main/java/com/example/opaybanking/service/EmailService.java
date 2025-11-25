package com.example.opaybanking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {

    private final Optional<JavaMailSender> mailSender;
    private final OtpService otpService;
    private final boolean skipEmail;

    public EmailService(@Lazy Optional<JavaMailSender> mailSender,
                        OtpService otpService,
                        @Value("${SKIP_EMAIL:false}") boolean skipEmail) {
        this.mailSender = mailSender;
        this.otpService = otpService;
        this.skipEmail = skipEmail;
    }

    private boolean shouldSend() {
        return !skipEmail && mailSender.isPresent();
    }

    @Async
    public void sendWelcomeEmail(String to, String firstName) throws MessagingException {
        if (!shouldSend()) {
            System.out.println("SKIP_EMAIL=true → Welcome email skipped for " + to);
            return;
        }
        sendHtmlEmail(to, "Welcome to Miles Bank", getWelcomeEmailHtml(firstName));
    }

    @Async
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        if (!shouldSend()) {
            System.out.println("OTP FOR " + to + " → " + otp + " (Email skipped - SKIP_EMAIL=true)");
            return;
        }

        String cachedOtp = otpService.getOtp(to);
        if (cachedOtp != null && !cachedOtp.equals(otp)) {
            System.out.println("OTP mismatch: Sent " + otp + " vs Cached " + cachedOtp);
        }

        sendHtmlEmail(to, "Your Miles Bank Verification Code", getOtpEmailHtml(otp));
    }

    @Async
    public void sendUpdateNotification(String to, String firstName) throws MessagingException {
        if (!shouldSend()) {
            System.out.println("SKIP_EMAIL=true → Update notification skipped for " + to);
            return;
        }
        sendHtmlEmail(to, "Account Updated", getUpdateNotificationHtml(firstName));
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.get().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom("Miles Bank <milesbrain280@gmail.com>");
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.get().send(message);
    }

    // YOUR BEAUTIFUL HTML TEMPLATES (keep exactly as below)
    public String getWelcomeEmailHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family:Arial;background:#f4f7fa;padding:20px">
            <div style="max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden">
                <div style="background:#1e90ff;color:white;padding:30px;text-align:center">
                    <h1>Miles Bank</h1>
                </div>
                <div style="padding:40px;text-align:center">
                    <h2>Welcome, %s!</h2>
                    <p>Your account is ready. Enjoy zero fees, instant transfers, and full control.</p>
                    <a href="#" style="background:#1e90ff;color:white;padding:14px 28px;text-decoration:none;border-radius:6px;display:inline-block;margin:20px">
                        Open Dashboard
                    </a>
                </div>
                <div style="background:#f8f9fa;padding:20px;font-size:12px;color:#666;text-align:center">
                    © 2025 Miles Bank • Built by Oyedokun Lukman
                </div>
            </div>
            </body></html>
            """.formatted(firstName);
    }

    public String getOtpEmailHtml(String otp) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family:Arial;background:#f4f7fa;padding:20px">
            <div style="max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden">
                <div style="background:linear-gradient(135deg,#1e90ff,#00bfff);color:white;padding:40px;text-align:center">
                    <h1>Miles Bank</h1>
                </div>
                <div style="padding:40px;text-align:center">
                    <h2>Your Verification Code</h2>
                    <div style="font-size:48px;font-weight:bold;letter-spacing:12px;background:#1e90ff;color:white;padding:20px;border-radius:12px;display:inline-block;margin:30px">
                        %s
                    </div>
                    <p><strong>Expires in 10 minutes</strong></p>
                    <p style="color:#e74c3c;font-weight:bold">Never share this code with anyone</p>
                </div>
                <div style="background:#f8f9fa;padding:20px;font-size:12px;color:#666;text-align:center">
                    © 2025 Miles Bank • Secure • Fast • Yours
                </div>
            </div>
            </body></html>
            """.formatted(otp);
    }

    public String getUpdateNotificationHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family:Arial;background:#f4f7fa;padding:20px">
            <div style="max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden">
                <div style="background:#1e90ff;color:white;padding:30px;text-align:center">
                    <h1>Miles Bank</h1>
                </div>
                <div style="padding:40px;text-align:center">
                    <h2>Account Updated</h2>
                    <p>Hi %s, your account details were successfully updated.</p>
                    <p>If you didn't do this, contact support immediately.</p>
                </div>
            </div>
            </body></html>
            """.formatted(firstName);
    }
}