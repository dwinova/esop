/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.security.error.dto;

import org.springframework.http.HttpStatus;

public class InvalidJwtAuthenticationErrorResponse extends SecurityErrorResponse {
	public InvalidJwtAuthenticationErrorResponse(final String message) {
		super(HttpStatus.UNAUTHORIZED.value(), message);
	}
}
