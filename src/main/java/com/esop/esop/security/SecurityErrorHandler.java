/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.esop.esop.security.error.ExpiredJwtAuthenticationError;
import com.esop.esop.security.error.InvalidJwtAuthenticationError;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class SecurityErrorHandler {
	private final com.esop.esop.common.error.ErrorMessageTranslator errorMessageTranslator;
	
	
	@ExceptionHandler(InvalidJwtAuthenticationError.class)
	public ResponseEntity<com.esop.esop.security.error.dto.InvalidJwtAuthenticationErrorResponse> handleInvalidJwtAuthenticationError(
			final InvalidJwtAuthenticationError e) {
		log.info("InvalidJwtAuthenticationError happened: {}.", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new com.esop.esop.security.error.dto.InvalidJwtAuthenticationErrorResponse(
					this.errorMessageTranslator.getErrorMessage("common.error.invalid-token")));
	}
	
	@ExceptionHandler(ExpiredJwtAuthenticationError.class)
	public ResponseEntity<com.esop.esop.security.error.dto.ExpiredJwtAuthenticationErrorResponse> handleExpiredJwtAuthenticationError(
			final ExpiredJwtAuthenticationError e) {
		log.info("ExpiredJwtAuthenticationError happened: {}.", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new com.esop.esop.security.error.dto.ExpiredJwtAuthenticationErrorResponse(
					this.errorMessageTranslator.getErrorMessage("common.error.expired-token")));
	}
}
