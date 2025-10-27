package com.esop.esop.email.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * AWS SES Configuration
 * Initializes SES client for sending emails
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.cloud.aws.ses.enabled", havingValue = "true")
public class AwsSesConfig {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Bean
    public SesClient sesClient() {
        try {
            log.info("🔧 Initializing AWS SES Client for region: {}", region);

            SesClient sesClient = SesClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)
                            )
                    )
                    .build();

            log.info("✅ AWS SES Client initialized successfully");
            return sesClient;
        } catch (Exception e) {
            log.error("❌ Failed to initialize AWS SES Client", e);
            throw new RuntimeException("AWS SES initialization failed", e);
        }
    }
}