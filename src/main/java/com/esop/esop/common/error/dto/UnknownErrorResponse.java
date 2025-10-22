/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.error.dto;

import lombok.Getter;
import lombok.NonNull;

import org.springframework.http.HttpStatus;

@Getter
public class UnknownErrorResponse extends BaseApplicationErrorResponse {
	public UnknownErrorResponse(@NonNull final String message) {
		super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
	}
}
