package com.sajee.auth.security.email;

public interface EmailSender {

    void sendVerificationEmail(String email, String verificationToken);
}