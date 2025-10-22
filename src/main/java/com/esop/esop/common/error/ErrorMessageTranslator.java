/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.error;

import java.util.Locale;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrorMessageTranslator {
	private final MessageSource messageSource;
	
	
	@NonNull
	public String getErrorMessage(@NonNull final String messageCode) {
		Locale locale = LocaleContextHolder.getLocale();
		return this.messageSource.getMessage(messageCode, null, locale);
	}
	
	@NonNull
	public String getErrorMessage(@NonNull final String messageCode, @NonNull final Object... placeHolders) {
		Locale locale = LocaleContextHolder.getLocale();
		return this.messageSource.getMessage(messageCode, placeHolders, locale);
	}
}
