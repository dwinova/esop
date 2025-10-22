/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.security;

import java.util.Date;
import java.util.NoSuchElementException;

import javax.crypto.SecretKey;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.apache.commons.lang3.StringUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

import com.esop.esop.security.error.InvalidTokenError;
import com.esop.esop.security.error.TokenDecodingError;
import com.esop.esop.security.error.TokenExpiredError;

@Slf4j
@Component
public class JwtTokenManager {
	@Value("${esop.jwt.secret}")
	private String jwtSecret;
	
	private static final String ROLE = "role";
	
	
	@NonNull
	public String generateAccessToken(final long memberId, @NonNull final com.esop.esop.member.model.Role role) {
		final long currentDateTime = System.currentTimeMillis();
		return Jwts.builder()
			.subject(String.valueOf(memberId))
			.claim(ROLE, role.name())
			.issuedAt(new Date(currentDateTime))
			.expiration(new Date(currentDateTime + JwtConst.ACCESS_TOKEN_EXPIRATION_TIME_IN_MILLISECONDS))
			.signWith(this.getSecretKey())
			.compact();
	}
	
	@NonNull
	public String parseAccessToken(@NonNull final String accessToken) {
		if (StringUtils.isNotBlank(accessToken) && StringUtils.startsWith(accessToken, JwtConst.BEARER_TOKEN_PREFIX)) {
			return accessToken.substring(JwtConst.BEARER_TOKEN_PREFIX.length());
		}
		throw new InvalidTokenError();
	}
	
	public long extractMemberId(@NonNull final String accessToken) {
		try {
			return Long.parseLong(Jwts.parser()
				.verifyWith(this.getSecretKey())
				.build()
				.parseSignedClaims(accessToken)
				.getPayload()
				.getSubject());
		} catch (NumberFormatException e) {
			log.warn("Error extracting memberId.", e);
			throw new InvalidTokenError();
		} catch (ExpiredJwtException e) {
			log.info("Jwt is invalid. {} exception happened.", e.getClass().getSimpleName());
			throw new TokenExpiredError();
		} catch (DecodingException e) {
			log.warn("Jwt error happened. {} exception happened.", e.getClass().getSimpleName());
			throw new TokenDecodingError();
		} catch (IllegalArgumentException | JwtException e) {
			log.warn("Jwt error happened", e);
			throw new InvalidTokenError();
		} catch (NoSuchElementException e) {
			log.warn("Error parsing Jwt claims.", e);
			throw new InvalidTokenError();
		}
	}
	
	@NonNull
	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}
}
