/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.email.controller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.esop.esop.email.service.AsyncEmailService;
import com.esop.esop.email.service.EmailService;

/**
 * Email Test Controller - ONLY FOR DEV/STAGING
 */
@Slf4j
@RestController
@RequestMapping("/api/dev/email")
@RequiredArgsConstructor
@Profile({
	"local",
	"stage"
})  // Only active in dev environments
@Tag(name = "Email Testing", description = "APIs for testing email functionality")
public class EmailTestController {
	
	private final EmailService emailService;
	
	private final AsyncEmailService asyncEmailService;
	
	
	@PostMapping("/test-simple")
	@Operation(summary = "Test simple email")
	public ResponseEntity<Map<String, String>> testSimpleEmail(
			@RequestParam String to,
			@RequestParam(defaultValue = "Test Simple Email") String subject,
			@RequestParam(defaultValue = "This is a test email from ESOP system") String body) {
		
		log.info("Testing simple email to: {}", to);
		
		try {
			emailService.sendSimpleEmail(to, subject, body);
			
			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "Simple email sent successfully",
					"to", to,
					"subject", subject));
		} catch (Exception e) {
			log.error("Failed to send test email", e);
			return ResponseEntity.internalServerError().body(Map.of(
					"status", "error",
					"message", "Failed to send email: " + e.getMessage()));
		}
	}
	
	@PostMapping("/test-html")
	@Operation(summary = "Test HTML email")
	public ResponseEntity<Map<String, String>> testHtmlEmail(
			@RequestParam String to,
			@RequestParam(defaultValue = "Test HTML Email") String subject) {
		
		log.info("Testing HTML email to: {}", to);
		
		String htmlBody = """
				<html>
				<head>
					<style>
						body { font-family: Arial, sans-serif; }
						.header { background-color: #007bff; color: white; padding: 20px; }
						.content { padding: 20px; }
						.button { background-color: #28a745; color: white; padding: 10px 20px;
								text-decoration: none; border-radius: 5px; display: inline-block; }
					</style>
				</head>
				<body>
					<div class="header">
						<h1>ESOP System Test Email</h1>
					</div>
					<div class="content">
						<h2>This is a test HTML email</h2>
						<p>If you can see this, HTML email is working correctly!</p>
						<p><a href="http://localhost:8080" class="button">Visit Dashboard</a></p>
					</div>
				</body>
				</html>
				""";
		
		try {
			emailService.sendHtmlEmail(to, subject, htmlBody);
			
			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "HTML email sent successfully",
					"to", to,
					"subject", subject));
		} catch (Exception e) {
			log.error("Failed to send HTML email", e);
			return ResponseEntity.internalServerError().body(Map.of(
					"status", "error",
					"message", "Failed to send email: " + e.getMessage()));
		}
	}
	
	@PostMapping("/test-password-reset")
	@Operation(summary = "Test password reset email")
	public ResponseEntity<Map<String, String>> testPasswordResetEmail(
			@RequestParam String to) {
		
		log.info("Testing password reset email to: {}", to);
		
		String testToken = "test-reset-token-" + System.currentTimeMillis();
		
		try {
			emailService.sendPasswordResetEmail(to, testToken);
			
			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "Password reset email sent successfully",
					"to", to,
					"resetToken", testToken,
					"resetLink", "http://localhost:3000/reset-password?token=" + testToken));
		} catch (Exception e) {
			log.error("Failed to send password reset email", e);
			return ResponseEntity.internalServerError().body(Map.of(
					"status", "error",
					"message", "Failed to send email: " + e.getMessage()));
		}
	}
	
	@PostMapping("/test-email-verification")
	@Operation(summary = "Test email verification")
	public ResponseEntity<Map<String, String>> testEmailVerification(
			@RequestParam String to) {
		
		log.info("Testing email verification to: {}", to);
		
		String testToken = "test-verify-token-" + System.currentTimeMillis();
		
		try {
			emailService.sendEmailVerificationEmail(to, testToken);
			
			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "Email verification sent successfully",
					"to", to,
					"verificationToken", testToken,
					"verificationLink", "http://localhost:3000/verify-email?token=" + testToken));
		} catch (Exception e) {
			log.error("Failed to send verification email", e);
			return ResponseEntity.internalServerError().body(Map.of(
					"status", "error",
					"message", "Failed to send email: " + e.getMessage()));
		}
	}
	
	@PostMapping("/test-transaction-notification")
	@Operation(summary = "Test transaction notification")
	public ResponseEntity<Map<String, String>> testTransactionNotification(
			@RequestParam String to,
			@RequestParam(defaultValue = "TXN-2025-001") String transactionCode,
			@RequestParam(defaultValue = "Successful") String status) {
		
		log.info("Testing transaction notification to: {}", to);
		
		try {
			emailService.sendTransactionNotification(to, transactionCode, status);
			
			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "Transaction notification sent successfully",
					"to", to,
					"transactionCode", transactionCode,
					"transactionStatus", status));
		} catch (Exception e) {
			log.error("Failed to send transaction notification", e);
			return ResponseEntity.internalServerError().body(Map.of(
					"status", "error",
					"message", "Failed to send email: " + e.getMessage()));
		}
	}
	
	@GetMapping("/send")
	public String triggerTestEmail() {
		asyncEmailService.testBulkSend();
		return "Started sending test emails...";
	}
}
