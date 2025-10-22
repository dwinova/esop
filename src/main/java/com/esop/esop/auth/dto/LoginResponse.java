/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
	@NonNull
	private final String accessToken;
	
	@NonNull
	private final String refreshToken;
	
	
	@NonNull
	public static LoginResponse from(@NonNull final String accessToken, @NonNull final String refreshToken) {
		return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
	}
}
