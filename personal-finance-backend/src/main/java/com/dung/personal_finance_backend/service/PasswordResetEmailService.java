package com.dung.personal_finance_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {
    private final JavaMailSender mailSender;
    private final String sender;

    public PasswordResetEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String sender
    ) {
        this.mailSender = mailSender;
        this.sender = sender;
    }

    public void sendResetLink(String recipient, String resetLink, long expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(recipient);
        message.setSubject("Reset your Personal Finance password");
        message.setText("""
                We received a request to reset your Personal Finance password.

                Open this link to choose a new password:
                %s

                This link expires in %d minutes and can only be used once.
                If you did not request this, you can ignore this email.
                """.formatted(resetLink, expirationMinutes));
        mailSender.send(message);
    }
}
