package com.esop.esop.file.service;

import com.esop.esop.file.exception.EncryptionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;

/**
 * Service for encrypting and decrypting data using Vault Transit secrets engine.
 * All encryption operations use AES-256 via Vault API.
 */
@Slf4j
@Service
public class VaultEncryptionService {

    private final VaultTemplate vaultTemplate;
    private final String transitKeyName;

    public VaultEncryptionService(
            VaultTemplate vaultTemplate,
            @org.springframework.beans.factory.annotation.Qualifier("transitKeyName") String transitKeyName) {
        this.vaultTemplate = vaultTemplate;
        this.transitKeyName = transitKeyName;
    }

    /**
     * Encrypts data using Vault Transit engine.
     * 
     * @param data Raw data to encrypt
     * @return Base64-encoded encrypted ciphertext with Vault prefix
     */
    public String encrypt(byte[] data) {
        try {
            // Create plaintext object from raw bytes
            Plaintext plaintext = Plaintext.of(data);
            
            // Encrypt using Vault Transit
            String ciphertext = vaultTemplate.opsForTransit()
                    .encrypt(transitKeyName, plaintext)
                    .getCiphertext();
            
            log.debug("Successfully encrypted {} bytes", data.length);
            return ciphertext;
            
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts data using Vault Transit engine.
     * 
     * @param ciphertext Encrypted data with Vault prefix (vault:v1:...)
     * @return Decrypted raw bytes
     */
    public byte[] decrypt(String ciphertext) {
        try {
            // Decrypt using Vault Transit
            Plaintext plaintext = vaultTemplate.opsForTransit()
                    .decrypt(transitKeyName, Ciphertext.of(ciphertext));
            
            byte[] decryptedData = plaintext.getPlaintext();
            log.debug("Successfully decrypted {} bytes", decryptedData.length);
            
            return decryptedData;
            
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Extracts Vault key version from ciphertext.
     * Format: vault:v1:base64data -> returns "v1"
     */
    public String extractKeyVersion(String ciphertext) {
        try {
            String[] parts = ciphertext.split(":");
            if (parts.length >= 2) {
                return parts[1];
            }
            return "unknown";
        } catch (Exception e) {
            log.warn("Could not extract key version from ciphertext", e);
            return "unknown";
        }
    }
}
