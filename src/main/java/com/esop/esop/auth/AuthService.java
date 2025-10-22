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

public interface AuthService {
	@NonNull
	com.esop.esop.auth.dto.LoginResponse refreshToken(@NonNull final String encryptedRefreshToken);
	
	@NonNull
	com.esop.esop.auth.dto.LoginResponse emailLogin(@NonNull final String email, @NonNull final String password);
}
