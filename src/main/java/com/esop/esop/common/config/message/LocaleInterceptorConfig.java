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

import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LocaleInterceptorConfig implements HandlerInterceptor {
	private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
	
	private static final String ENGLISH = "en";
	
	private static final List<String> ACCEPTED_LANGUAGE = List.of(ENGLISH);
	
	
	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
			final Object handler) {
		final String acceptLanguage = request.getHeader(ACCEPT_LANGUAGE_HEADER);
		if (StringUtils.isNotBlank(acceptLanguage) && ACCEPTED_LANGUAGE.contains(acceptLanguage)) {
			Locale locale = Locale.forLanguageTag(acceptLanguage);
			LocaleContextHolder.setLocale(locale);
		} else {
			LocaleContextHolder.setLocale(Locale.ENGLISH);
		}
		
		return true;
	}
}
