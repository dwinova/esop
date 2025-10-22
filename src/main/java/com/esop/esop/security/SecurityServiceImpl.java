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

import lombok.AllArgsConstructor;
import lombok.NonNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esop.esop.auth.AccessTokenRedisRepository;
import com.esop.esop.member.MemberRepository;
import com.esop.esop.security.error.InvalidTokenError;
import com.esop.esop.security.error.MemberNotFoundError;

@Service
@AllArgsConstructor
public class SecurityServiceImpl implements SecurityService {
	private final MemberRepository memberRepository;
	
	private final AccessTokenRedisRepository accessTokenRedisRepository;
	
	
	@NonNull
	@Transactional(readOnly = true)
	@Override
	public com.esop.esop.member.model.Member findMemberById(final long memberId) {
		return this.memberRepository.findById(memberId).orElseThrow(MemberNotFoundError::new);
	}
	
	@Transactional(readOnly = true)
	@Override
	public void validateAccessToken(final long memberId, @NonNull final String accessToken) {
		final String foundMemberId = this.accessTokenRedisRepository.getAccessToken(memberId, accessToken);
		
		try {
			if (Long.parseLong(foundMemberId) != memberId) {
				throw new InvalidTokenError();
			}
		} catch (NumberFormatException e) {
			throw new InvalidTokenError();
		}
	}
	
}
