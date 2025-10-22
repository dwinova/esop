/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.verification;

import lombok.NonNull;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.esop.esop.common.redis.AbstractRedisRepository;
import com.esop.esop.common.redis.RedisConst;

@Component
public class PhoneVerificationRedisRepository extends AbstractRedisRepository<String, String> {
	public PhoneVerificationRedisRepository(final RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate);
	}
	
	public void savePhoneVerificationCode(@NonNull final String key, @NonNull final String value) {
		this.set(key, value, RedisConst.DEFAULT_PHONE_OTP_TTL_IN_SECONDS);
	}
}
