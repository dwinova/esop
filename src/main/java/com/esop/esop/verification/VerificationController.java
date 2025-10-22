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
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.esop.esop.verification.dto.GetMobilePhoneOTPResponse;
import com.esop.esop.verification.error.dto.InvalidMobilePhoneFormatErrorResponse;
import com.esop.esop.verification.error.dto.TooManyPhoneRetryAttemptErrorResponse;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/verification")
public class VerificationController {
	private final VerificationService verificationService;
	
	
	@Operation(summary = "Get an one-time password for a mobile phone")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Get one-time password for a mobile phone successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetMobilePhoneOTPResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid mobile phone or retry limit exceeded", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {
			InvalidMobilePhoneFormatErrorResponse.class,
			TooManyPhoneRetryAttemptErrorResponse.class
		}))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema)),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema))
	})
	@GetMapping("/phone/{mobile_phone}/otp")
	public ResponseEntity<GetMobilePhoneOTPResponse> generatePhoneVerificationCode(
			@PathVariable("mobile_phone") final String mobilePhone) {
		final String otpCode = this.verificationService.generatePhoneVerificationCode(mobilePhone);
		return ResponseEntity.ok(GetMobilePhoneOTPResponse.from(mobilePhone, otpCode));
	}
	
}
