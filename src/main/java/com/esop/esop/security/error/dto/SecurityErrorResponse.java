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

import com.esop.esop.common.error.dto.BaseApplicationErrorResponse;

public class SecurityErrorResponse extends BaseApplicationErrorResponse {
	public SecurityErrorResponse(int status, String message) {
		super(status, message);
	}
}
