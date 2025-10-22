/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.esop.esop.auth.error.dto.InvalidEmailAndPasswordErrorResponse;
import com.esop.esop.auth.error.dto.InvalidEmailFormatErrorResponse;
import com.esop.esop.auth.error.dto.InvalidRefreshTokenErrorResponse;
import com.esop.esop.auth.error.dto.MemberNotFoundErrorResponse;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@RestControllerAdvice
public class AuthErrorHandler {
	private final com.esop.esop.common.error.ErrorMessageTranslator errorMessageTranslator;
	
	
	@ExceptionHandler(com.esop.esop.auth.error.InvalidRefreshTokenError.class)
	public ResponseEntity<InvalidRefreshTokenErrorResponse> handleInvalidRefreshTokenError(
			final com.esop.esop.auth.error.InvalidRefreshTokenError invalidRefreshTokenError) {
		log.info("InvalidRefreshTokenError happened.", invalidRefreshTokenError);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new InvalidRefreshTokenErrorResponse(
					this.errorMessageTranslator.getErrorMessage("auth.error.invalid-refresh-token")));
	}
	
	@ExceptionHandler(com.esop.esop.auth.error.InvalidEmailAndPasswordError.class)
	public ResponseEntity<InvalidEmailAndPasswordErrorResponse> handleInvalidEmailAndPasswordError(
			final com.esop.esop.auth.error.InvalidEmailAndPasswordError invalidEmailAndPasswordError) {
		log.info("InvalidEmailAndPasswordError happened.", invalidEmailAndPasswordError);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new InvalidEmailAndPasswordErrorResponse(
					this.errorMessageTranslator.getErrorMessage("auth.error.invalid-email-and-password")));
	}
	
	@ExceptionHandler(com.esop.esop.auth.error.InvalidEmailFormatError.class)
	public ResponseEntity<InvalidEmailFormatErrorResponse> handleInvalidEmailFormatError(
			final com.esop.esop.auth.error.InvalidEmailFormatError invalidEmailFormatError) {
		log.info("InvalidEmailFormatError happened.", invalidEmailFormatError);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new InvalidEmailFormatErrorResponse(
					this.errorMessageTranslator.getErrorMessage("auth.error.invalid-email-format")));
	}
	
	@ExceptionHandler(com.esop.esop.auth.error.MemberNotFoundError.class)
	public ResponseEntity<MemberNotFoundErrorResponse> handleMemberNotFoundError(
			final com.esop.esop.auth.error.MemberNotFoundError memberNotFoundError) {
		log.info("MemberNotFoundError happened.", memberNotFoundError);
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new MemberNotFoundErrorResponse(
					this.errorMessageTranslator.getErrorMessage("common.error.member-not-found")));
	}
}
