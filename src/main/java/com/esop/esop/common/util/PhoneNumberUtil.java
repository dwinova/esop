/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.apache.commons.lang3.StringUtils;

import com.esop.esop.verification.error.InvalidMobilePhoneFormatError;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PhoneNumberUtil {
	private static final String PHONE_REGEX = "^(\\+\\d{1,14}|0\\d{9,14})$";
	
	
	public static void validatePhoneFormat(@NonNull final String mobilePhone) {
		if (StringUtils.isBlank(mobilePhone)) {
			throw new InvalidMobilePhoneFormatError(mobilePhone);
		}
		
		final Pattern pattern = Pattern.compile(PHONE_REGEX);
		final Matcher matcher = pattern.matcher(mobilePhone.trim());
		
		if (!matcher.matches()) {
			throw new InvalidMobilePhoneFormatError(mobilePhone);
		}
	}
}
