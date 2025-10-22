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

import java.util.Random;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PhoneVerificationCodeGenerator {
	private static final Random RANDOM = new Random();
	
	
	@NonNull
	public static String generatePhoneVerificationCode() {
		return String.format("%06d", RANDOM.nextInt(1000000));
	}
}
