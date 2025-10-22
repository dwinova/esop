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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.esop.esop.auth.error.FailedToGenerateRefreshTokenError;
import com.esop.esop.auth.error.InvalidRefreshTokenError;

@Slf4j
@Component
public class RefreshTokenManager {
	private final TextEncryptor encryptor;
	
	private final ObjectMapper objectMapper;
	
	
	public RefreshTokenManager(@Value("${esop.security.aes256.password}") final String password,
			@Value("${esop.security.aes256.salt}") final String salt,
			final ObjectMapper objectMapper) {
		this.encryptor = Encryptors.text(password, salt);
		this.objectMapper = objectMapper;
	}
	
	@NonNull
	public String generateRefreshToken(final long memberId) {
		try {
			final String refreshToken = this.objectMapper.writeValueAsString(
					com.esop.esop.auth.dto.RefreshToken.builder().memberId(memberId)
						.generatedAt(System.currentTimeMillis()).build());
			return this.encryptor.encrypt(refreshToken);
		} catch (final JsonProcessingException e) {
			log.error("Failed to generate refresh token", e);
			throw new FailedToGenerateRefreshTokenError();
		}
	}
	
	@NonNull
	public com.esop.esop.auth.dto.RefreshToken decryptRefreshToken(@NonNull final String encryptedToken) {
		try {
			final String decryptedToken = this.encryptor.decrypt(encryptedToken);
			return this.objectMapper.readValue(decryptedToken, com.esop.esop.auth.dto.RefreshToken.class);
		} catch (final Exception e) {
			log.info("Failed to decrypt refreshToken: {}", encryptedToken, e);
			throw new InvalidRefreshTokenError(encryptedToken);
		}
	}
}
