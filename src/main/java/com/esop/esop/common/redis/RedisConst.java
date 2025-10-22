/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.redis;

public interface RedisConst {
	/** Access Token */
	long DEFAULT_ACCESS_TOKEN_TTL_IN_SECONDS = 86400;
	
	/** Access Token */
	
	/** Refresh Token */
	long DEFAULT_REFRESH_TOKEN_TTL_IN_SECONDS = 86400 * 90;
	
	/** Access Token */
	
	/** Phone Verification Config */
	long DEFAULT_PHONE_OTP_TTL_IN_SECONDS = 300;
	
	long MINIMUM_RETRY_TIME_IN_SECONDS = 60;
	
	long MINIMUM_ALLOWED_RETRY_TIME = DEFAULT_PHONE_OTP_TTL_IN_SECONDS - MINIMUM_RETRY_TIME_IN_SECONDS;
	/** Phone Verification Config */
}
