package com.esop.esop.email.service;

import lombok.NonNull;

public interface EmailService {

    /**
     * Send simple text email
     */
    void sendSimpleEmail(@NonNull String to, @NonNull String subject, @NonNull String body);

    /**
     * Send HTML email
     */
    void sendHtmlEmail(@NonNull String to, @NonNull String subject, @NonNull String htmlBody);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(@NonNull String email, @NonNull String resetToken);

    /**
     * Send email verification email
     */
    void sendEmailVerificationEmail(@NonNull String email, @NonNull String verificationToken);

    /**
     * Send transaction notification
     */
    void sendTransactionNotification(@NonNull String email, @NonNull String transactionCode,
                                     @NonNull String status);
}