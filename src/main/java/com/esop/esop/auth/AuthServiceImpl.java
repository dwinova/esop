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
import lombok.NonNull;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esop.esop.member.MemberRepository;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final AccessTokenRedisRepository accessTokenRedisRepository;
	
	private final RefreshTokenRedisRepository refreshTokenRedisRepository;
	
	private final MemberRepository memberRepository;
	
	private final com.esop.esop.security.JwtTokenManager jwtTokenManager;
	
	private final RefreshTokenManager refreshTokenManager;
	
	private final PasswordEncoder passwordEncoder;
	
	
	@NonNull
	@Transactional
	@Override
	public com.esop.esop.auth.dto.LoginResponse refreshToken(@NonNull final String encryptedRefreshToken) {
		final com.esop.esop.auth.dto.RefreshToken refreshToken =
				this.refreshTokenManager.decryptRefreshToken(encryptedRefreshToken);
		final com.esop.esop.member.model.Member foundMember =
				this.memberRepository.findById(refreshToken.getMemberId())
					.orElseThrow(com.esop.esop.auth.error.MemberNotFoundError::new);
		this.validateRefreshToken(foundMember.getId(), encryptedRefreshToken);
		final String accessToken = this.jwtTokenManager.generateAccessToken(foundMember.getId(), foundMember.getRole());
		this.accessTokenRedisRepository.saveAccessToken(accessToken, foundMember.getId());
		return com.esop.esop.auth.dto.LoginResponse.from(accessToken, encryptedRefreshToken);
	}
	
	@NonNull
	@Transactional
	@Override
	public com.esop.esop.auth.dto.LoginResponse emailLogin(@NonNull final String email,
			@NonNull final String password) {
		final com.esop.esop.member.model.Member foundMember = this.memberRepository.findMemberByEmail(email)
			.orElseThrow(com.esop.esop.auth.error.MemberNotFoundError::new);
		this.validateCredential(password, foundMember.getPassword());
		final String accessToken = this.jwtTokenManager.generateAccessToken(foundMember.getId(), foundMember.getRole());
		final String refreshToken = this.refreshTokenManager.generateRefreshToken(foundMember.getId());
		this.accessTokenRedisRepository.saveAccessToken(accessToken, foundMember.getId());
		this.refreshTokenRedisRepository.saveRefreshToken(refreshToken, foundMember.getId());
		return com.esop.esop.auth.dto.LoginResponse.from(accessToken, refreshToken);
	}
	
	private void validateCredential(@NonNull final String requestPassword, @NonNull final String encryptedPassword) {
		if (!passwordEncoder.matches(requestPassword, encryptedPassword)) {
			throw new com.esop.esop.auth.error.InvalidEmailAndPasswordError();
		}
	}
	
	private void validateRefreshToken(final long memberId, @NonNull final String refreshToken) {
		if (this.refreshTokenRedisRepository.getRefreshToken(memberId, refreshToken) == null) {
			throw new com.esop.esop.auth.error.InvalidRefreshTokenError(refreshToken);
		}
	}
}
