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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;

import org.springframework.stereotype.Component;

@Component
public class EmailProgressTracker {
	
	private final Map<String, EmailProgress> progressMap = new ConcurrentHashMap<>();
	
	
	public void startTracking(String taskId, int totalEmails) {
		progressMap.put(taskId, new EmailProgress(totalEmails));
	}
	
	public void incrementSuccess(String taskId) {
		EmailProgress progress = progressMap.get(taskId);
		if (progress != null) {
			progress.getSuccessCount().incrementAndGet();
		}
	}
	
	public void incrementFail(String taskId) {
		EmailProgress progress = progressMap.get(taskId);
		if (progress != null) {
			progress.getFailCount().incrementAndGet();
		}
	}
	
	public EmailProgress getProgress(String taskId) {
		return progressMap.get(taskId);
	}
	
	public void removeTracking(String taskId) {
		progressMap.remove(taskId);
	}
	
	
	@Data
	public static class EmailProgress {
		private final int totalEmails;
		
		private final AtomicInteger successCount = new AtomicInteger(0);
		
		private final AtomicInteger failCount = new AtomicInteger(0);
		
		
		public EmailProgress(int totalEmails) {
			this.totalEmails = totalEmails;
		}
		
		public int getProcessed() {
			return successCount.get() + failCount.get();
		}
		
		public double getProgressPercentage() {
			if (totalEmails == 0)
				return 0;
			return (double) getProcessed() / totalEmails * 100;
		}
		
		public boolean isCompleted() {
			return getProcessed() >= totalEmails;
		}
	}
}
