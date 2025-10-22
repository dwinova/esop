/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.security;

public interface JwtConst {
	String AUTHORIZATION_HEADER = "Authorization";
	
	String BEARER_TOKEN_PREFIX = "Bearer ";
	
	long ACCESS_TOKEN_EXPIRATION_TIME_IN_MILLISECONDS =
			com.esop.esop.common.redis.RedisConst.DEFAULT_ACCESS_TOKEN_TTL_IN_SECONDS * 1000;
}
