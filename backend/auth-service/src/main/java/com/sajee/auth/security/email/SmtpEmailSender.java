package com.sajee.auth.security.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String email, String verificationToken) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Verify your SecureID account");

        message.setText("""
                Welcome to SecureID!
                
                Please verify your email address using the following link:
                
                http://localhost:5173/verify-email?token=%s
                
                This verification link will expire according to the configured
                verification token lifetime.
                
                If you did not create this account, you can safely ignore this email.
                """.formatted(verificationToken));

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Reset your SecureID password");

        message.setText("""
                We received a request to reset your SecureID password.
                
                Please use the following link to reset your password:
                
                http://localhost:5173/reset-password?token=%s
                
                This password reset link will expire according to the configured
                password reset token lifetime.
                
                If you did not request a password reset, you can safely ignore this email.
                """.formatted(resetToken));

        mailSender.send(message);
    }
}
