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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.esop.esop.common.error.dto.BadRequestErrorResponse;
import com.esop.esop.common.error.dto.HttpMethodNotSupportedErrorResponse;
import com.esop.esop.common.error.dto.UnknownErrorResponse;

@Slf4j
@Order
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonErrorHandler {
	private final ErrorMessageTranslator errorMessageTranslator;
	
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BadRequestErrorResponse> handleMethodArgumentNotValidException(
			final MethodArgumentNotValidException methodArgumentNotValidException) {
		log.info("MethodArgumentNotValidException happened.", methodArgumentNotValidException);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new BadRequestErrorResponse(methodArgumentNotValidException.getFieldError().getDefaultMessage()));
	}
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<HttpMethodNotSupportedErrorResponse> handleHttpRequestMethodNotSupportedError(
			final Exception exception) {
		log.info("HttpRequestMethodNotSupportedException happened.", exception);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new HttpMethodNotSupportedErrorResponse(
					this.errorMessageTranslator.getErrorMessage("common.error.http-method-not-supported")));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<UnknownErrorResponse> handleUnknownExceptionError(final Exception exception) {
		log.error("Unknown error happened.", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new UnknownErrorResponse(this.errorMessageTranslator.getErrorMessage("common.error.unknown-error")));
	}
}
