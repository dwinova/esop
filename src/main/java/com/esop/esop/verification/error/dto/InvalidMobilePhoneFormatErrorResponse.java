/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.verification.error.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class InvalidMobilePhoneFormatErrorResponse extends VerificationErrorResponse {
	
	private final InvalidMobilePhoneFormatErrorData errorData;
	
	
	@Getter
	@AllArgsConstructor
	private static class InvalidMobilePhoneFormatErrorData {
		private final String invalidPhone;
	}
	
	
	public InvalidMobilePhoneFormatErrorResponse(final String invalidPhone, final String message) {
		super(HttpStatus.BAD_REQUEST.value(), message);
		this.errorData = new InvalidMobilePhoneFormatErrorData(invalidPhone);
	}
}
