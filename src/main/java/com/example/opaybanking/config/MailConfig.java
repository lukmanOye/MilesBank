package com.example.opaybanking.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnProperty(name = "SKIP_EMAIL", havingValue = "true", matchIfMissing = true)
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }
}
