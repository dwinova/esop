/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.email.config;

import java.util.concurrent.Executor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {
	
	@Bean(name = "emailTaskExecutor")
	public Executor emailTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(2000); 
		executor.setThreadNamePrefix("email-async-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(120);
		executor.initialize();
		
		log.info("✅ Email Task Executor initialized: core={}, max={}",
				executor.getCorePoolSize(), executor.getMaxPoolSize());
		return executor;
	}
}
