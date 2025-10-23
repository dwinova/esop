-- Create file_metadata table for encrypted file storage (MySQL)
CREATE TABLE IF NOT EXISTS file_metadata (
                                             id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                             filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    size BIGINT NOT NULL,
    checksum VARCHAR(128) NOT NULL COMMENT 'SHA-256 checksum of original file',
    storage_path VARCHAR(512) NOT NULL COMMENT 'Object path in MinIO',
    vault_key_version VARCHAR(64) COMMENT 'Vault Transit key version used for encryption',
    uploaded_by VARCHAR(128) NOT NULL,
    uploaded_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_storage_path UNIQUE (storage_path)
    ) COMMENT='Stores metadata for encrypted files in MinIO';

-- Create indexes for faster queries
CREATE INDEX idx_uploaded_by ON file_metadata(uploaded_by);
CREATE INDEX idx_uploaded_at ON file_metadata(uploaded_at);