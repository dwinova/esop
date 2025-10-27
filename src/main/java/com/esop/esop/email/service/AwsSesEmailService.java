package com.esop.esop.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import com.esop.esop.email.exception.*;

/**
 * AWS SES Email Service Implementation
 * Handles all email sending via Amazon SES
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.aws.ses.enabled", havingValue = "true")
public class AwsSesEmailService implements EmailService {

    private final SesClient sesClient;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.sender-name}")
    private String senderName;

    @Value("${email.password-reset-link}")
    private String passwordResetLink;

    @Value("${email.email-verification-link}")
    private String emailVerificationLink;

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            log.info("📧 Sending simple email to: {}", to);

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(formatSender())
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .text(Content.builder().data(body).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);
            log.info("✅ Email sent successfully. MessageId: {}", response.messageId());

        } catch (SesException e) {
            log.error("❌ Failed to send email to: {}", to, e);
            throw new EmailSendException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            log.info("📧 Sending HTML email to: {}", to);

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(formatSender())
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);
            log.info("✅ HTML email sent successfully. MessageId: {}", response.messageId());

        } catch (SesException e) {
            log.error("❌ Failed to send HTML email to: {}", to, e);
            throw new EmailSendException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            log.info("🔐 Sending password reset email to: {}", email);

            String resetUrl = passwordResetLink + resetToken;
            String subject = "Password Reset Request - ESOP";
            String htmlBody = buildPasswordResetHtml(email, resetUrl);

            sendHtmlEmail(email, subject, htmlBody);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to: {}", email, e);
            throw new EmailSendException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendEmailVerificationEmail(String email, String verificationToken) {
        try {
            log.info("✉️ Sending email verification to: {}", email);

            String verificationUrl = emailVerificationLink + verificationToken;
            String subject = "Verify Your Email - ESOP";
            String htmlBody = buildEmailVerificationHtml(email, verificationUrl);

            sendHtmlEmail(email, subject, htmlBody);

        } catch (Exception e) {
            log.error("❌ Failed to send email verification to: {}", email, e);
            throw new EmailSendException("Failed to send email verification", e);
        }
    }

    @Override
    public void sendTransactionNotification(String email, String transactionCode, String status) {
        try {
            log.info("💰 Sending transaction notification to: {}", email);

            String subject = "Transaction Notification - ESOP";
            String htmlBody = buildTransactionNotificationHtml(email, transactionCode, status);

            sendHtmlEmail(email, subject, htmlBody);

        } catch (Exception e) {
            log.error("❌ Failed to send transaction notification to: {}", email, e);
            throw new EmailSendException("Failed to send transaction notification", e);
        }
    }

    // Helper Methods
    private String formatSender() {
        return String.format("%s <%s>", senderName, fromEmail);
    }

    private String buildPasswordResetHtml(String email, String resetUrl) {
        return String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<h2>Password Reset Request</h2>" +
                        "<p>Hello,</p>" +
                        "<p>We received a request to reset your password. Click the link below to proceed:</p>" +
                        "<p><a href='%s' style='background-color: #007bff; color: white; padding: 10px 20px; " +
                        "text-decoration: none; border-radius: 5px; display: inline-block;'>" +
                        "Reset Password</a></p>" +
                        "<p>This link expires in 1 hour.</p>" +
                        "<p>If you didn't request this, ignore this email.</p>" +
                        "<p>Best regards,<br/>ESOP Team</p>" +
                        "</body>" +
                        "</html>",
                resetUrl
        );
    }

    private String buildEmailVerificationHtml(String email, String verificationUrl) {
        return String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<h2>Email Verification</h2>" +
                        "<p>Hello,</p>" +
                        "<p>Please verify your email address by clicking the link below:</p>" +
                        "<p><a href='%s' style='background-color: #28a745; color: white; padding: 10px 20px; " +
                        "text-decoration: none; border-radius: 5px; display: inline-block;'>" +
                        "Verify Email</a></p>" +
                        "<p>This link expires in 24 hours.</p>" +
                        "<p>Best regards,<br/>ESOP Team</p>" +
                        "</body>" +
                        "</html>",
                verificationUrl
        );
    }

    private String buildTransactionNotificationHtml(String email, String transactionCode, String status) {
        String statusColor = status.equals("Successful") ? "#28a745" : "#dc3545";
        return String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<h2>Transaction Notification</h2>" +
                        "<p>Hello,</p>" +
                        "<p>Your transaction has been updated:</p>" +
                        "<p><strong>Transaction Code:</strong> %s</p>" +
                        "<p><strong>Status:</strong> <span style='color: %s; font-weight: bold;'>%s</span></p>" +
                        "<p>For more details, please log in to your account.</p>" +
                        "<p>Best regards,<br/>ESOP Team</p>" +
                        "</body>" +
                        "</html>",
                transactionCode, statusColor, status
        );
    }
}