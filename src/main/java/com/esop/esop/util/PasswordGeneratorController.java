package com.esop.esop.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * DEVELOPMENT ONLY - Remove in production!
 * Utility endpoint to generate BCrypt password hashes.
 */
@Profile({"local", "stage"}) // Only active in non-prod environments
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class PasswordGeneratorController {

    private final PasswordEncoder passwordEncoder;

    /**
     * Generate BCrypt hash for a password.
     * GET /api/dev/hash-password?password=yourpassword
     */
    @GetMapping("/hash-password")
    public ResponseEntity<PasswordHashResponse> hashPassword(
            @RequestParam String password) {

        String hash = passwordEncoder.encode(password);
        boolean verification = passwordEncoder.matches(password, hash);

        String sqlInsert = String.format(
                "INSERT INTO members (email, password, role, created_at, updated_at) " +
                        "VALUES ('your@email.com', '%s', 'USER', NOW(), NOW());",
                hash
        );

        return ResponseEntity.ok(new PasswordHashResponse(
                password,
                hash,
                verification,
                sqlInsert
        ));
    }

    /**
     * Test if a password matches a hash.
     * POST /api/dev/verify-password
     */
    @PostMapping("/verify-password")
    public ResponseEntity<PasswordVerifyResponse> verifyPassword(
            @RequestBody PasswordVerifyRequest request) {

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                request.getHash()
        );

        return ResponseEntity.ok(new PasswordVerifyResponse(
                matches,
                matches ? "✅ Password matches hash" : "❌ Password does NOT match hash"
        ));
    }

    @Data
    @AllArgsConstructor
    public static class PasswordHashResponse {
        private String originalPassword;
        private String bcryptHash;
        private boolean verificationTest;
        private String sqlInsertStatement;
    }

    @Data
    public static class PasswordVerifyRequest {
        private String password;
        private String hash;
    }

    @Data
    @AllArgsConstructor
    public static class PasswordVerifyResponse {
        private boolean matches;
        private String message;
    }
}