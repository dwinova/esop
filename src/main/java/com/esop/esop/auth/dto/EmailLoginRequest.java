/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.auth.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

import com.esop.esop.common.util.EmailUtil;

@Builder
@Data
public class EmailLoginRequest {
	@NotBlank(message = "email can not be blank")
	private String email;
	
	@NotBlank(message = "password can not be blank")
	private String password;
	
	
	public EmailLoginRequest(final String email, final String password) {
		EmailUtil.validateEmailFormat(email);
		
		this.email = email;
		this.password = password;
	}
}
