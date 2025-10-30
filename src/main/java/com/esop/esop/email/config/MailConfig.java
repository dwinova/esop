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

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConditionalOnProperty(name = "spring.cloud.aws.ses.enabled", havingValue = "false", matchIfMissing = true)
public class MailConfig {

	@Bean
	public org.springframework.mail.javamail.JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("localhost");
		mailSender.setPort(25);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.starttls.enable", "false");
		props.put("mail.smtp.connectiontimeout", "5000");
		props.put("mail.smtp.timeout", "3000");
		props.put("mail.smtp.writetimeout", "5000");

		return mailSender;
	}

	@org.springframework.context.annotation.Primary
	@Bean
	public java.util.function.Supplier<org.springframework.mail.javamail.JavaMailSender> mailSenderSupplier() {
		return () -> {
			JavaMailSenderImpl sender = new JavaMailSenderImpl();
			sender.setHost("mailpit-esop");
			sender.setPort(1025); // đổi sang 25 nếu dùng smtp4dev
			
			Properties props = sender.getJavaMailProperties();
			props.put("mail.smtp.auth", "false");
			props.put("mail.smtp.starttls.enable", "false");
			props.put("mail.smtp.connectiontimeout", "10000");
			props.put("mail.smtp.timeout", "8000");
			props.put("mail.smtp.writetimeout", "10000");
			props.put("mail.debug", "true");
			return sender;
		};
	}
}
