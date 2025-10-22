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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.esop.esop.security.error.ExpiredJwtAuthenticationError;
import com.esop.esop.security.error.InvalidJwtAuthenticationError;
import com.esop.esop.security.error.InvalidTokenError;
import com.esop.esop.security.error.MemberNotFoundError;
import com.esop.esop.security.error.TokenDecodingError;
import com.esop.esop.security.error.TokenExpiredError;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final AuthenticationEntryPoint authenticationEntryPoint;
	
	private final JwtTokenManager jwtTokenManager;
	
	private final SecurityService securityService;
	
	
	public JwtAuthenticationFilter(final AuthenticationEntryPoint authenticationEntryPoint,
			final JwtTokenManager jwtTokenManager,
			final SecurityService securityService) {
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.jwtTokenManager = jwtTokenManager;
		this.securityService = securityService;
	}
	
	@Override
	protected void doFilterInternal(final HttpServletRequest request,
			final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException {
		final String bearerToken = request.getHeader(JwtConst.AUTHORIZATION_HEADER);
		if (StringUtils.isNotBlank(bearerToken) && StringUtils.startsWith(bearerToken, JwtConst.BEARER_TOKEN_PREFIX)) {
			final String accessToken = this.jwtTokenManager.parseAccessToken(bearerToken);
			try {
				final long memberId = this.jwtTokenManager.extractMemberId(accessToken);
				this.securityService.validateAccessToken(memberId, accessToken);
				this.setupSecurityContext(request, memberId);
			} catch (MemberNotFoundError | TokenDecodingError | InvalidTokenError e) {
				this.authenticationEntryPoint.commence(request, response,
						new InvalidJwtAuthenticationError(accessToken));
				return;
			} catch (TokenExpiredError e) {
				this.authenticationEntryPoint.commence(request, response,
						new ExpiredJwtAuthenticationError(accessToken));
				return;
			}
		}
		
		filterChain.doFilter(request, response);
	}
	
	private void setupSecurityContext(final HttpServletRequest request, final long memberId) {
		if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
			final com.esop.esop.member.model.Member foundMember = this.securityService.findMemberById(memberId);
			final com.esop.esop.member.model.Role memberRole = foundMember.getRole();
			final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(memberRole.name()));
			final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
					new UsernamePasswordAuthenticationToken(foundMember, null, authorities);
			usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		}
	}
}
