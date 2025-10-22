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
import org.springframework.stereotype.Component;

@Component
public class AccessTokenRedisRepository extends com.esop.esop.common.redis.AbstractRedisRepository<String, String> {
	public AccessTokenRedisRepository(RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate);
	}
	
	@NonNull
	public String getAccessToken(final long memberId, @NonNull final String accessToken) {
		return this.get(AuthTokenRedisKeyGenerator.generateAccessTokenKey(memberId, accessToken));
	}
	
	public void saveAccessToken(@NonNull final String accessToken, final long memberId) {
		this.set(AuthTokenRedisKeyGenerator.generateAccessTokenKey(memberId, accessToken), String.valueOf(memberId),
				com.esop.esop.common.redis.RedisConst.DEFAULT_ACCESS_TOKEN_TTL_IN_SECONDS);
	}
}
