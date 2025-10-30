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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.esop.esop.email.model.EmailBatchResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncEmailService {
	
	private final EmailService emailService;
	
	
	/**
	 * Gửi email bất đồng bộ cho 1 người
	 */
	@Async("emailTaskExecutor")
	public CompletableFuture<Boolean> sendEmailAsync(String email, String subject, String body) {
		try {
			emailService.sendHtmlEmail(email, subject, body);
			log.debug("📨 Email sent successfully to: {}", email);
			return CompletableFuture.completedFuture(true);
		} catch (Exception e) {
			log.error("❌ Failed to send email to: {}", email, e);
			return CompletableFuture.completedFuture(false);
		}
	}
	
	/**
	 * Gửi 1 batch email (100–200 email)
	 */
	@Async("emailTaskExecutor")
	public CompletableFuture<EmailBatchResult> sendBulkEmailsAsync(
			List<String> emails,
			String subject,
			String bodyTemplate) {
		
		log.info("🚀 Starting batch with {} recipients", emails.size());
		
		int successCount = 0;
		int failCount = 0;
		
		for (String email : emails) {
			try {
				String personalizedBody = bodyTemplate.replace("{{email}}", email);
				emailService.sendHtmlEmail(email, subject, personalizedBody);
				successCount++;
				
				if (successCount % 50 == 0) {
					log.info("📬 [{}] Sent {} emails so far...", Thread.currentThread().getName(), successCount);
				}
				
				// Rate limiting: tránh dồn connection khi test cục bộ
				Thread.sleep(80);
				
			} catch (Exception e) {
				log.warn("⚠️ Failed to send email to {}", email, e);
				failCount++;
			}
		}
		
		EmailBatchResult result = EmailBatchResult.builder()
			.totalEmails(emails.size())
			.successCount(successCount)
			.failCount(failCount)
			.build();
		
		log.info("✅ Batch done: {} success / {} fail (rate: {}%)",
				successCount, failCount, String.format("%.2f", result.getSuccessRate()));
		
		return CompletableFuture.completedFuture(result);
	}
	
	/**
	 * Gửi email hàng loạt 600–1000 người bằng cách chia batch song song
	 */
	public CompletableFuture<List<EmailBatchResult>> sendEmailsInBatches(
			List<String> allEmails, String subject, String bodyTemplate) {
		
		int batchSize = 100; // có thể tăng lên 200 nếu máy đủ mạnh
		List<CompletableFuture<EmailBatchResult>> futures = new ArrayList<>();
		
		log.info("📦 Preparing to send {} emails in batches of {}", allEmails.size(), batchSize);
		
		for (int i = 0; i < allEmails.size(); i += batchSize) {
			int end = Math.min(i + batchSize, allEmails.size());
			List<String> batch = allEmails.subList(i, end);
			futures.add(sendBulkEmailsAsync(batch, subject, bodyTemplate));
		}
		
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
			.thenApply(v -> futures.stream()
				.map(CompletableFuture::join)
				.collect(Collectors.toList()));
	}
	
	/**
	 * Hàm test: gửi 1000 email giả
	 */
	public void testBulkSend() {
		List<String> fakeEmails = IntStream.range(0, 1000)
			.mapToObj(i -> "user" + i + "@example.com")
			.collect(Collectors.toList());
		
		String subject = "ESOP Test Email";
		String body = "<p>Hello {{email}},<br>Welcome to ESOP!</p>";
		
		sendEmailsInBatches(fakeEmails, subject, body).thenAccept(results -> {
			int totalSuccess = results.stream().mapToInt(EmailBatchResult::getSuccessCount).sum();
			int totalFail = results.stream().mapToInt(EmailBatchResult::getFailCount).sum();
			
			log.info("🎉 Bulk test completed: {} success / {} fail / rate: {}%",
					totalSuccess, totalFail,
					String.format("%.2f",
							((double) totalSuccess / (totalSuccess + totalFail)) * 100));
		});
	}
}
