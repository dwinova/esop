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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthTokenRedisKeyGenerator {
	private static final String ACCESS_TOKEN_KEY_FORMAT = "member_id:%d:access_token:%s";
	
	private static final String REFRESH_TOKEN_KEY_FORMAT = "member_id:%d:refresh_token:%s";
	
	
	@NonNull
	public static String generateAccessTokenKey(final long memberId, @NonNull final String accessToken) {
		return String.format(ACCESS_TOKEN_KEY_FORMAT, memberId, accessToken);
	}
	
	@NonNull
	public static String generateRefreshTokenKey(final long memberId, @NonNull final String refreshToken) {
		return String.format(REFRESH_TOKEN_KEY_FORMAT, memberId, refreshToken);
	}
}
