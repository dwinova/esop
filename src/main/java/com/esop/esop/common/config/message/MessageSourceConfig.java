/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.config.message;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageSourceConfig {
	private static final String DEFAULT_MESSAGE_ENCODING = "UTF-8";
	
	private static final String MESSAGE_SOURCE_BASE_NAME = "messages/messages";
	
	
	@Bean
	public ResourceBundleMessageSource messageSource() {
		final ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		source.setBasenames(MESSAGE_SOURCE_BASE_NAME);
		source.setDefaultEncoding(DEFAULT_MESSAGE_ENCODING);
		source.setFallbackToSystemLocale(false);
		source.setUseCodeAsDefaultMessage(true);
		source.setAlwaysUseMessageFormat(true);
		source.setCacheSeconds(-1);
		
		return source;
	}
}
