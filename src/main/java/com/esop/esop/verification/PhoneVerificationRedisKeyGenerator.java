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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PhoneVerificationRedisKeyGenerator {
	private static final String PHONE_VERIFICATION_KEY_FORMAT = "phone_verification:%s";
	
	
	@NonNull
	public static String generate(@NonNull final String mobilePhone) {
		return String.format(PHONE_VERIFICATION_KEY_FORMAT, mobilePhone);
	}
}
