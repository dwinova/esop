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

import lombok.NonNull;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenRedisRepository extends com.esop.esop.common.redis.AbstractRedisRepository<String, String> {
	public RefreshTokenRedisRepository(RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate);
	}
	
	@Nullable
	public String getRefreshToken(final long memberId, @NonNull final String refreshToken) {
		return this.get(AuthTokenRedisKeyGenerator.generateRefreshTokenKey(memberId, refreshToken));
	}
	
	public void saveRefreshToken(@NonNull final String refreshToken, final long memberId) {
		this.set(AuthTokenRedisKeyGenerator.generateRefreshTokenKey(memberId, refreshToken), String.valueOf(memberId),
				com.esop.esop.common.redis.RedisConst.DEFAULT_REFRESH_TOKEN_TTL_IN_SECONDS);
	}
}
