package com.esop.esop.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

/**
 * Vault configuration for Transit secrets engine.
 * Connects to HashiCorp Vault for encryption/decryption operations.
 */
@Configuration
public class VaultConfig {

    @Value("${vault.uri}")
    private String vaultUri;

    @Value("${vault.token}")
    private String vaultToken;

    @Value("${vault.transit.key-name}")
    private String transitKeyName;

    /**
     * Creates VaultTemplate bean for interacting with Vault API.
     */
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultUri));
        TokenAuthentication authentication = new TokenAuthentication(vaultToken);
        return new VaultTemplate(endpoint, authentication);
    }

    /**
     * Provides the Transit key name used for encryption/decryption.
     */
    @Bean
    public String transitKeyName() {
        return transitKeyName;
    }
}
