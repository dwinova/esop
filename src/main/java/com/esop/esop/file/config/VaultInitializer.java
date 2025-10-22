package com.esop.esop.file.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.Map;

/**
 * Automatically initializes Vault Transit engine on application startup.
 *
 * This component:
 * 1. Enables Transit secrets engine if not already enabled
 * 2. Creates AES-256 encryption key if not exists
 * 3. Configures key with secure settings
 *
 * Runs once when Spring Boot starts up.
 */
@Slf4j
@Component
@Order(1)
public class VaultInitializer {

    private final VaultTemplate vaultTemplate;
    private final String transitKeyName;

    public VaultInitializer(
            VaultTemplate vaultTemplate,
            @Qualifier("transitKeyName") String transitKeyName) {
        this.vaultTemplate = vaultTemplate;
        this.transitKeyName = transitKeyName;
    }

    /**
     * Initializes Vault Transit engine when application is ready.
     * This method runs automatically after Spring Boot startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeVault() {
        try {
            log.info("🔐 Initializing Vault Transit engine...");

            // Give Vault a moment to be fully ready
            Thread.sleep(2000);

            // Step 1: Enable Transit secrets engine (if not already enabled)
            enableTransitEngine();

            // Step 2: Create or verify encryption key
            createOrVerifyKey();

            log.info("✅ Vault Transit engine ready!");

        } catch (Exception e) {
            log.error("❌ Failed to initialize Vault Transit engine", e);
            log.warn("⚠️  File encryption features will not work until Vault is properly configured");
            log.warn("⚠️  Please run these commands manually:");
            log.warn("   docker exec -it vault-esop vault secrets enable transit");
            log.warn("   docker exec -it vault-esop vault write -f transit/keys/{}", transitKeyName);
        }
    }

    /**
     * Enables Transit secrets engine if not already enabled.
     */
    private void enableTransitEngine() {
        try {
            // Check if transit is already mounted
            VaultResponse mountsResponse = vaultTemplate.read("sys/mounts");

            if (mountsResponse != null && mountsResponse.getData() != null) {
                Map<String, Object> mounts = mountsResponse.getData();

                if (mounts.containsKey("transit/")) {
                    log.info("✓ Transit engine already enabled");
                    return;
                }
            }

            // Transit not enabled, enable it
            log.info("📝 Enabling Transit secrets engine...");

            VaultMount vaultMount = VaultMount.builder()
                    .type("transit")
                    .description("Transit secrets engine for file encryption")
                    .build();

            vaultTemplate.opsForSys().mount("transit", vaultMount);

            log.info("✓ Transit engine enabled successfully");

            // Give Vault time to fully enable the engine
            Thread.sleep(1000);

        } catch (Exception e) {
            log.warn("⚠️  Could not enable Transit engine automatically: {}", e.getMessage());
            throw new RuntimeException("Failed to enable Transit engine", e);
        }
    }

    /**
     * Creates encryption key if it doesn't exist, or verifies it exists.
     */
    private void createOrVerifyKey() {
        try {
            // Try to read key configuration
            String keyPath = "transit/keys/" + transitKeyName;
            VaultResponse response = vaultTemplate.read(keyPath);

            if (response != null && response.getData() != null) {
                log.info("✓ Encryption key '{}' already exists", transitKeyName);
                logKeyInfo(response);
                return;
            }

        } catch (Exception e) {
            // Key doesn't exist, will create below
            log.debug("Key does not exist yet, will create: {}", e.getMessage());
        }

        // Create the key
        createKey();
    }

    /**
     * Creates a new AES-256 encryption key in Vault Transit.
     */
    private void createKey() {
        try {
            log.info("📝 Creating encryption key '{}'...", transitKeyName);

            String createKeyPath = "transit/keys/" + transitKeyName;

            // Create key with secure settings
            Map<String, Object> keyConfig = Map.of(
                    "type", "aes256-gcm96",
                    "convergent_encryption", false,
                    "exportable", false,
                    "allow_plaintext_backup", false
            );

            vaultTemplate.write(createKeyPath, keyConfig);

            log.info("✓ Base key created");

            // Give Vault time to create the key
            Thread.sleep(500);

            // Configure additional key settings
            String configPath = "transit/keys/" + transitKeyName + "/config";
            Map<String, Object> additionalConfig = Map.of(
                    "deletion_allowed", false,
                    "min_decryption_version", 1,
                    "min_encryption_version", 1
            );

            try {
                vaultTemplate.write(configPath, additionalConfig);
                log.info("✓ Key configuration updated");
            } catch (Exception e) {
                log.debug("Could not update key config (key still works): {}", e.getMessage());
            }

            log.info("✅ Encryption key '{}' created successfully!", transitKeyName);

        } catch (Exception e) {
            log.error("❌ Failed to create encryption key '{}'", transitKeyName, e);
            throw new RuntimeException("Vault key creation failed", e);
        }
    }

    /**
     * Logs information about the encryption key.
     */
    private void logKeyInfo(VaultResponse response) {
        try {
            Map<String, Object> data = response.getData();
            if (data != null) {
                Object type = data.get("type");
                Object latestVersion = data.get("latest_version");
                Object deletionAllowed = data.get("deletion_allowed");

                log.info("  → Type: {}", type);
                log.info("  → Latest version: {}", latestVersion);
                log.info("  → Deletion allowed: {}", deletionAllowed);
            }
        } catch (Exception e) {
            log.debug("Could not log key info", e);
        }
    }
}