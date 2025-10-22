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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import com.esop.esop.auth.error.dto.InvalidEmailFormatErrorResponse;
import com.esop.esop.auth.error.dto.InvalidRefreshTokenErrorResponse;
import com.esop.esop.auth.error.dto.MemberNotFoundErrorResponse;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	
	
	@Operation(summary = "Refresh access token")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Refresh access token successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.esop.esop.auth.dto.LoginResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid refresh token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvalidRefreshTokenErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "Member not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberNotFoundErrorResponse.class)))
	})
	@PostMapping("/token")
	public ResponseEntity<com.esop.esop.auth.dto.LoginResponse> refreshToken(
			@Valid @RequestBody final com.esop.esop.auth.dto.TokenRequest request) {
		return ResponseEntity.ok(this.authService.refreshToken(request.getRefreshToken()));
	}
	
	@Operation(summary = "Login by email and password")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Login successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.esop.esop.auth.dto.LoginResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid email format", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvalidEmailFormatErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "Member not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberNotFoundErrorResponse.class)))
	})
	@PostMapping("/email-login")
	public ResponseEntity<com.esop.esop.auth.dto.LoginResponse> emailLogin(
			@Valid @RequestBody final com.esop.esop.auth.dto.EmailLoginRequest request) {
		return ResponseEntity.ok(this.authService.emailLogin(request.getEmail(), request.getPassword()));
	}
}
