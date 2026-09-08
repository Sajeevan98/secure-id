package com.sajee.auth.security.email;

public interface EmailSender {

    void sendVerificationEmail(String email, String verificationToken);

    void sendPasswordResetEmail(String email, String resetToken);
}