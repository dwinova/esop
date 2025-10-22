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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private static final String ROLE_USER = com.esop.esop.member.model.Role.USER.name();
	
	private static final String ROLE_ADMIN = com.esop.esop.member.model.Role.ADMIN.name();
	
	private final AuthenticationEntryPoint authenticationEntryPoint;
	
	private final JwtTokenManager jwtTokenManager;
	
	private final SecurityService securityService;
	
	
	public SecurityConfig(final AuthenticationEntryPoint authenticationEntryPoint,
			final JwtTokenManager jwtTokenManager,
			final SecurityService securityService) {
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.jwtTokenManager = jwtTokenManager;
		this.securityService = securityService;
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).sessionManagement(
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authorizeHttpRequests(authorize -> authorize
			// No authentication paths
			.requestMatchers("/swagger/**").permitAll()
			.requestMatchers("/api/auth/token").permitAll()
			.requestMatchers("/api/auth/email-login").permitAll()
			.requestMatchers("/swagger-ui/**").permitAll()
			.requestMatchers("/v3/api-docs/**").permitAll()
			// Required ROLE_ADMIN paths
			.requestMatchers("/api/admin/**").hasAuthority(ROLE_ADMIN)
			// Required ROLE_USER paths
			.requestMatchers("/api/user/**").hasAnyAuthority(ROLE_USER)
			// Require ROLE_ADMIN or ROLE_USER paths
			.requestMatchers("/api/verification/**").hasAnyAuthority(ROLE_ADMIN, ROLE_USER)
			.anyRequest().authenticated())
			.addFilterBefore(new JwtAuthenticationFilter(this.authenticationEntryPoint, this.jwtTokenManager,
					this.securityService), UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(
			final AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
