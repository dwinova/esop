/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class GetMobilePhoneOTPResponse {
	@NonNull
	private final String mobilePhone;
	
	@NonNull
	private final String otpCode;
	
	
	@NonNull
	public static GetMobilePhoneOTPResponse from(@NonNull final String mobilePhone, @NonNull final String otpCode) {
		return GetMobilePhoneOTPResponse.builder().mobilePhone(mobilePhone).otpCode(otpCode).build();
	}
}
