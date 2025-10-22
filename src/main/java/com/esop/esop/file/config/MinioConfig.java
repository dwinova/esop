package com.esop.esop.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO S3-compatible storage configuration.
 * Initializes MinIO client and ensures bucket exists.
 */
@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * Creates MinioClient bean and ensures bucket exists.
     */
    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();

            // Check if bucket exists, create if not
            boolean bucketExists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Created MinIO bucket: {}", bucketName);
            }

            log.info("MinIO client initialized successfully");
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client", e);
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }

    @Bean
    public String minioBucketName() {
        return bucketName;
    }
}
