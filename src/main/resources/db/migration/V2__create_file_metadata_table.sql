-- Create file_metadata table for encrypted file storage
CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    size BIGINT NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    storage_path VARCHAR(512) NOT NULL,
    vault_key_version VARCHAR(64),
    uploaded_by VARCHAR(128) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_storage_path UNIQUE (storage_path)
);

-- Create indexes for faster queries
CREATE INDEX idx_uploaded_by ON file_metadata(uploaded_by);
CREATE INDEX idx_uploaded_at ON file_metadata(uploaded_at);

-- Add comments
COMMENT ON TABLE file_metadata IS 'Stores metadata for encrypted files in MinIO';
COMMENT ON COLUMN file_metadata.checksum IS 'SHA-256 checksum of original file';
COMMENT ON COLUMN file_metadata.vault_key_version IS 'Vault Transit key version used for encryption';
