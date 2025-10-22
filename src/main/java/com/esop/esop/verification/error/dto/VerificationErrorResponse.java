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

import lombok.Getter;

import com.esop.esop.common.error.dto.BaseApplicationErrorResponse;

@Getter
public class VerificationErrorResponse extends BaseApplicationErrorResponse {
	public VerificationErrorResponse(int status, final String message) {
		super(status, message);
	}
}
