/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.esop.esop.common.error.ErrorMessageTranslator;
import com.esop.esop.verification.error.InvalidMobilePhoneFormatError;
import com.esop.esop.verification.error.TooManyPhoneRetryAttemptError;
import com.esop.esop.verification.error.dto.InvalidMobilePhoneFormatErrorResponse;
import com.esop.esop.verification.error.dto.TooManyPhoneRetryAttemptErrorResponse;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@RestControllerAdvice
public class VerificationErrorHandler {
	private final ErrorMessageTranslator errorMessageTranslator;
	
	
	@ExceptionHandler(InvalidMobilePhoneFormatError.class)
	public ResponseEntity<InvalidMobilePhoneFormatErrorResponse> handleInvalidMobilePhoneFormatError(
			final InvalidMobilePhoneFormatError invalidMobilePhoneFormatError) {
		log.info("InvalidMobilePhoneFormatError happened.", invalidMobilePhoneFormatError);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new InvalidMobilePhoneFormatErrorResponse(invalidMobilePhoneFormatError.getInvalidPhone(),
					this.errorMessageTranslator.getErrorMessage("verification.error.invalid-mobile-phone-format")));
	}
	
	@ExceptionHandler(TooManyPhoneRetryAttemptError.class)
	public ResponseEntity<TooManyPhoneRetryAttemptErrorResponse> handleTooManyPhoneRetryAttemptError(
			final TooManyPhoneRetryAttemptError tooManyPhoneRetryAttemptError) {
		log.info("TooManyPhoneRetryAttemptError happened.", tooManyPhoneRetryAttemptError);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new TooManyPhoneRetryAttemptErrorResponse(
					this.errorMessageTranslator.getErrorMessage("verification.error.too-many-phone-retry-attempt",
							tooManyPhoneRetryAttemptError.getTimeLeft())));
	}
}
