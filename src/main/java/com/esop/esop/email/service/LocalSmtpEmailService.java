/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.email.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Local SMTP Email Service (dùng SMTP4Dev)
 * Chỉ active khi EMAIL_SES_ENABLED=false
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.cloud.aws.ses.enabled", havingValue = "false", matchIfMissing = true)
public class LocalSmtpEmailService implements EmailService {
	
	private final JavaMailSender mailSender;
	
	@Value("${email.from}")
	private String fromEmail;
	
	@Value("${email.sender-name}")
	private String senderName;

	@Value("${email.password-reset-link}")
	private String passwordResetLink;
	
	@Value("${email.email-verification-link}")
	private String emailVerificationLink;
	
	
	public LocalSmtpEmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	@Override
	public void sendSimpleEmail(String to, String subject, String body) {
		try {
			log.info("📧 [LOCAL] Sending simple email to: {}", to);
			
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			
			helper.setFrom(String.format("%s <%s>", senderName, fromEmail));
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, false); // Plain text
			
			mailSender.send(message);
			
			log.info("✅ [LOCAL] Simple email sent successfully to: {}", to);
			
		} catch (Exception e) {
			log.error("❌ [LOCAL] Failed to send email to: {}", to, e);
			throw new com.esop.esop.email.exception.EmailSendException("Failed to send email", e);
		}
	}
	
	@Override
	public void sendHtmlEmail(String to, String subject, String htmlBody) {
		try {
			log.info("📧 [LOCAL] Sending HTML email to: {}", to);
			
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			
			helper.setFrom(String.format("%s <%s>", senderName, fromEmail));
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true); // HTML
			
			mailSender.send(message);
			
			log.info("✅ [LOCAL] HTML email sent successfully to: {}", to);
			
		} catch (Exception e) {
			log.error("❌ [LOCAL] Failed to send HTML email to: {}", to, e);
			throw new com.esop.esop.email.exception.EmailSendException("Failed to send HTML email", e);
		}
	}
	
	@Override
	public void sendPasswordResetEmail(String email, String resetToken) {
		try {
			log.info("🔐 [LOCAL] Sending password reset email to: {}", email);
			
			String resetUrl = passwordResetLink + resetToken;
			String subject = "Password Reset Request - ESOP";
			String htmlBody = buildPasswordResetHtml(email, resetUrl);
			
			sendHtmlEmail(email, subject, htmlBody);
			
		} catch (Exception e) {
			log.error("❌ [LOCAL] Failed to send password reset email", e);
			throw new com.esop.esop.email.exception.EmailSendException("Failed to send password reset email", e);
		}
	}
	
	@Override
	public void sendEmailVerificationEmail(String email, String verificationToken) {
		try {
			log.info("✉️ [LOCAL] Sending email verification to: {}", email);
			
			String verificationUrl = emailVerificationLink + verificationToken;
			String subject = "Verify Your Email - ESOP";
			String htmlBody = buildEmailVerificationHtml(email, verificationUrl);
			
			sendHtmlEmail(email, subject, htmlBody);
			
		} catch (Exception e) {
			log.error("❌ [LOCAL] Failed to send email verification", e);
			throw new com.esop.esop.email.exception.EmailSendException("Failed to send email verification", e);
		}
	}
	
	@Override
	public void sendTransactionNotification(String email, String transactionCode, String status) {
		try {
			log.info("💰 [LOCAL] Sending transaction notification to: {}", email);
			
			String subject = "Transaction Notification - ESOP";
			String htmlBody = buildTransactionNotificationHtml(email, transactionCode, status);
			
			sendHtmlEmail(email, subject, htmlBody);
			
		} catch (Exception e) {
			log.error("❌ [LOCAL] Failed to send transaction notification", e);
			throw new com.esop.esop.email.exception.EmailSendException("Failed to send transaction notification", e);
		}
	}
	
	// HTML Builder Methods (same as AwsSesEmailService)
	private String buildPasswordResetHtml(String email, String resetUrl) {
		return String.format(
				"<html>" +
						"<head><style>body{font-family:Arial,sans-serif;}</style></head>" +
						"<body>" +
						"<h2>Password Reset Request</h2>" +
						"<p>Hello,</p>" +
						"<p>We received a request to reset your password. Click the link below to proceed:</p>" +
						"<p><a href='%s' style='background-color:#007bff;color:white;padding:10px 20px;" +
						"text-decoration:none;border-radius:5px;display:inline-block;'>Reset Password</a></p>" +
						"<p>This link expires in 1 hour.</p>" +
						"<p>If you didn't request this, ignore this email.</p>" +
						"<p>Best regards,<br/>ESOP Team</p>" +
						"</body></html>",
				resetUrl);
	}
	
	private String buildEmailVerificationHtml(String email, String verificationUrl) {
		return String.format(
				"<html>" +
						"<head><style>body{font-family:Arial,sans-serif;}</style></head>" +
						"<body>" +
						"<h2>Email Verification</h2>" +
						"<p>Hello,</p>" +
						"<p>Please verify your email address by clicking the link below:</p>" +
						"<p><a href='%s' style='background-color:#28a745;color:white;padding:10px 20px;" +
						"text-decoration:none;border-radius:5px;display:inline-block;'>Verify Email</a></p>" +
						"<p>This link expires in 24 hours.</p>" +
						"<p>Best regards,<br/>ESOP Team</p>" +
						"</body></html>",
				verificationUrl);
	}
	
	private String buildTransactionNotificationHtml(String email, String transactionCode, String status) {
		String statusColor = status.equals("Successful") ? "#28a745" : "#dc3545";
		return String.format(
				"<html>" +
						"<head><style>body{font-family:Arial,sans-serif;}</style></head>" +
						"<body>" +
						"<h2>Transaction Notification</h2>" +
						"<p>Hello,</p>" +
						"<p>Your transaction has been updated:</p>" +
						"<p><strong>Transaction Code:</strong> %s</p>" +
						"<p><strong>Status:</strong> <span style='color:%s;font-weight:bold;'>%s</span></p>" +
						"<p>For more details, please log in to your account.</p>" +
						"<p>Best regards,<br/>ESOP Team</p>" +
						"</body></html>",
				transactionCode, statusColor, status);
	}
}
