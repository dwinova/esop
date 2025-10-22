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

import lombok.AllArgsConstructor;
import lombok.NonNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class VerificationServiceImpl implements VerificationService {
	private final PhoneVerificationRedisRepository phoneVerificationRedisRepository;
	
	
	@NonNull
	@Override
	@Transactional
	public String generatePhoneVerificationCode(@NonNull final String mobilePhone) {
		com.esop.esop.common.util.PhoneNumberUtil.validatePhoneFormat(mobilePhone);
		final String key = PhoneVerificationRedisKeyGenerator.generate(mobilePhone);
		this.validateRetryAttempt(key);
		
		final String verificationCode = PhoneVerificationCodeGenerator.generatePhoneVerificationCode();
		this.phoneVerificationRedisRepository.savePhoneVerificationCode(key, verificationCode);
		return verificationCode;
	}
	
	private void validateRetryAttempt(@NonNull final String key) {
		final Long recordTtl = this.phoneVerificationRedisRepository.getRecordTtl(key);
		if (recordTtl != null && recordTtl > com.esop.esop.common.redis.RedisConst.MINIMUM_ALLOWED_RETRY_TIME) {
			throw new com.esop.esop.verification.error.TooManyPhoneRetryAttemptError(
					recordTtl - com.esop.esop.common.redis.RedisConst.MINIMUM_ALLOWED_RETRY_TIME);
		}
	}
}
