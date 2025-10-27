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

-- ============================================================================
-- DVPP (Decathlon Value Participation Program) Database Schema
-- Flyway Migration Scripts
-- Version: V1.0__Initial_Schema.sql
-- ============================================================================

-- ============================================================================
-- 1. CORE REFERENCE TABLES
-- ============================================================================

CREATE TABLE roles (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL UNIQUE,
                       description TEXT,
                       is_system_role BOOLEAN DEFAULT FALSE,
                       permissions JSON NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(50),
                       last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       last_modified_by VARCHAR(50),
                       INDEX idx_name (name)
);

CREATE TABLE departments (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description TEXT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             INDEX idx_name (name)
);

CREATE TABLE locations (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(100) NOT NULL UNIQUE,
                           city VARCHAR(100),
                           country VARCHAR(100),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           INDEX idx_name (name)
);

CREATE TABLE employment_types (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  name VARCHAR(50) NOT NULL UNIQUE,
                                  description TEXT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  INDEX idx_name (name)
);

-- ============================================================================
-- 2. ADMIN & USER MANAGEMENT
-- ============================================================================

CREATE TABLE admins (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        fed_id VARCHAR(50) NOT NULL UNIQUE,
                        full_name VARCHAR(200) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(100) NOT NULL,
                        is_active BOOLEAN DEFAULT TRUE,
                        last_login_at TIMESTAMP NULL,
                        failed_login_attempts INT DEFAULT 0,
                        account_locked_until TIMESTAMP NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by VARCHAR(50),
                        last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (role) REFERENCES roles(name),
                        INDEX idx_fed_id (fed_id),
                        INDEX idx_email (email),
                        INDEX idx_is_active (is_active)
);

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       fed_id VARCHAR(50) NOT NULL UNIQUE,
                       full_name VARCHAR(200) NOT NULL,
                       company_email VARCHAR(255) UNIQUE,
                       personal_email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       mobile_number VARCHAR(20),
                       date_of_birth DATE,
                       nationality VARCHAR(50),
                       tax_id VARCHAR(50),
                       nominee VARCHAR(200),

    -- Employment Information
                       department_id BIGINT,
                       job_title VARCHAR(100),
                       employment_type_id BIGINT,
                       location_id BIGINT,
                       date_of_joining DATE NOT NULL,
                       employment_status ENUM('Active', 'Terminated', 'Retired', 'Revoked', 'Standby') DEFAULT 'Active',
                       termination_date DATE NULL,

    -- DVPP Program Status
                       plan_participation_status ENUM('Not Enrolled', 'Enrolled', 'Withdrawn') DEFAULT 'Not Enrolled',
                       eligibility_status ENUM('Eligible', 'Ineligible', 'Pending Review') DEFAULT 'Pending Review',
                       enrollment_status VARCHAR(50),

    -- Bank Information
                       bank_name VARCHAR(100),
                       account_name VARCHAR(200),
                       bank_number VARCHAR(50),

    -- System Configuration
                       preferred_language VARCHAR(10) DEFAULT 'en',
                       system_role VARCHAR(50) DEFAULT 'Employee',
                       pending_new_email VARCHAR(255) NULL,
                       email_verification_token VARCHAR(255) NULL,
                       email_verification_expires TIMESTAMP NULL,

    -- Metadata
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(50),
                       last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       last_modified_by VARCHAR(50),

                       FOREIGN KEY (department_id) REFERENCES departments(id),
                       FOREIGN KEY (employment_type_id) REFERENCES employment_types(id),
                       FOREIGN KEY (location_id) REFERENCES locations(id),
                       INDEX idx_fed_id (fed_id),
                       INDEX idx_personal_email (personal_email),
                       INDEX idx_plan_status (plan_participation_status),
                       INDEX idx_employment_status (employment_status),
                       INDEX idx_department (department_id),
                       INDEX idx_created_at (created_at)
);

-- ============================================================================
-- 3. SETTINGS & CONFIGURATION
-- ============================================================================

CREATE TABLE unit_value_history (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    effective_date DATE NOT NULL,
                                    value_eur DECIMAL(10, 2) NOT NULL,
                                    status ENUM('Active', 'Scheduled', 'Archived', 'Pending Validation') DEFAULT 'Pending Validation',
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    created_by VARCHAR(50),
                                    notes TEXT,
                                    UNIQUE KEY unique_effective_date (effective_date),
                                    INDEX idx_status (status),
                                    INDEX idx_effective_date (effective_date)
);

CREATE TABLE exchange_rate_history (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       effective_date DATE NOT NULL,
                                       rate_value DECIMAL(15, 2) NOT NULL COMMENT 'VND per EUR',
                                       source ENUM('Manual', 'API', 'Import') DEFAULT 'Manual',
                                       status ENUM('Active', 'Scheduled', 'Archived', 'Pending Validation') DEFAULT 'Pending Validation',
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       created_by VARCHAR(50),
                                       notes TEXT,
                                       UNIQUE KEY unique_effective_date (effective_date),
                                       INDEX idx_status (status),
                                       INDEX idx_effective_date (effective_date),
                                       INDEX idx_created_at (created_at)
);

CREATE TABLE salary_percentage_history (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           effective_date DATE NOT NULL,
                                           percentage_value INT NOT NULL COMMENT 'Max salary percentage for investment',
                                           status ENUM('Active', 'Scheduled', 'Archived', 'Pending Validation') DEFAULT 'Pending Validation',
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           created_by VARCHAR(50),
                                           notes TEXT,
                                           UNIQUE KEY unique_effective_date (effective_date),
                                           INDEX idx_status (status),
                                           INDEX idx_effective_date (effective_date)
);

CREATE TABLE subscription_settings (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       subscription_start DATE NOT NULL,
                                       subscription_end DATE NOT NULL,
                                       is_active BOOLEAN DEFAULT TRUE,
                                       notes TEXT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       created_by VARCHAR(50),
                                       UNIQUE KEY unique_period (subscription_start, subscription_end)
);

CREATE TABLE topup_settings (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                from_units DECIMAL(10, 2) NOT NULL,
                                to_units DECIMAL(10, 2) NOT NULL,
                                free_units INT NOT NULL,
                                expiration_date DATE NOT NULL,
                                is_active BOOLEAN DEFAULT TRUE,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                created_by VARCHAR(50),
                                INDEX idx_active (is_active),
                                INDEX idx_expiration (expiration_date)
);

CREATE TABLE early_redemption_reasons (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          title VARCHAR(100) NOT NULL UNIQUE,
                                          description TEXT NOT NULL,
                                          requires_documentation BOOLEAN DEFAULT TRUE,
                                          allowed_document_types VARCHAR(255) DEFAULT 'pdf,jpg,png',
                                          is_active BOOLEAN DEFAULT TRUE,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          created_by VARCHAR(50)
);

CREATE TABLE tax_rate_settings (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   user_id BIGINT NOT NULL,
                                   tax_rate DECIMAL(5, 2) NOT NULL,
                                   effective_from DATE NOT NULL,
                                   expiration_date DATE NOT NULL,
                                   notes TEXT,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   created_by VARCHAR(50),
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                   INDEX idx_user_id (user_id),
                                   INDEX idx_expiration_date (expiration_date)
);

-- ============================================================================
-- 4. TRANSACTIONS
-- ============================================================================

CREATE TABLE transactions (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              transaction_code VARCHAR(50) NOT NULL UNIQUE,
                              user_id BIGINT NOT NULL,
                              type ENUM('Subscription', 'Redemption', 'Early Redemption', 'Free Unit Grant', 'Forced Redemption') NOT NULL,
                              status ENUM('Submitted', 'Payment', 'Processing', 'Tax Outdated', 'Successful', 'Rejected', 'Canceled') DEFAULT 'Submitted',

    -- Transaction Details
                              transaction_date DATE NOT NULL,
                              unit_price DECIMAL(15, 2) NOT NULL,
                              purchased_units DECIMAL(10, 2),
                              topup_units DECIMAL(10, 2) DEFAULT 0,
                              total_units DECIMAL(10, 2) NOT NULL,
                              gross_amount DECIMAL(15, 2) NOT NULL,
                              tax_rate DECIMAL(5, 2) DEFAULT 0,
                              tax_amount DECIMAL(15, 2) DEFAULT 0,
                              net_amount DECIMAL(15, 2) NOT NULL,

    -- Vesting Information
                              vesting_date DATE NULL,

    -- Redemption Specific
                              reason VARCHAR(100) NULL,
                              payment_method ENUM('QR Code', 'Manual Transfer', 'Bank Account') NULL,

    -- Early Redemption Specific
                              early_redemption_reason_id BIGINT NULL,
                              early_redemption_approved_by BIGINT NULL,

    -- Validation Information
                              validated_by BIGINT NULL,
                              validated_at TIMESTAMP NULL,
                              rejected_by BIGINT NULL,
                              rejected_at TIMESTAMP NULL,
                              rejection_reason TEXT NULL,
                              admin_notes JSON NULL,

    -- Metadata
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              created_by VARCHAR(50),
                              last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              last_modified_by VARCHAR(50),

                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              FOREIGN KEY (early_redemption_reason_id) REFERENCES early_redemption_reasons(id),
                              FOREIGN KEY (validated_by) REFERENCES admins(id),
                              FOREIGN KEY (rejected_by) REFERENCES admins(id),
                              FOREIGN KEY (early_redemption_approved_by) REFERENCES admins(id),
                              INDEX idx_user_id (user_id),
                              INDEX idx_status (status),
                              INDEX idx_type (type),
                              INDEX idx_transaction_date (transaction_date),
                              INDEX idx_vesting_date (vesting_date),
                              INDEX idx_created_at (created_at)
);

CREATE TABLE transaction_attachments (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         transaction_id BIGINT NOT NULL,
                                         file_name VARCHAR(255) NOT NULL,
                                         file_path VARCHAR(500) NOT NULL,
                                         file_type VARCHAR(50),
                                         file_size INT,
                                         uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         uploaded_by VARCHAR(50),
                                         FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
                                         INDEX idx_transaction_id (transaction_id)
);

CREATE TABLE transaction_validation_history (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                transaction_id BIGINT NOT NULL,
                                                old_status VARCHAR(50),
                                                new_status VARCHAR(50),
                                                changed_by BIGINT,
                                                change_reason TEXT,
                                                changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
                                                FOREIGN KEY (changed_by) REFERENCES admins(id),
                                                INDEX idx_transaction_id (transaction_id),
                                                INDEX idx_changed_at (changed_at)
);

-- ============================================================================
-- 5. ENROLLMENT & PARTICIPATION
-- ============================================================================

CREATE TABLE enrollment_requests (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     user_id BIGINT NOT NULL,
                                     status ENUM('Request', 'Processing', 'Enrolled', 'Withdrawn', 'Rejected') DEFAULT 'Request',
                                     esignature_status VARCHAR(50),
                                     esignature_token VARCHAR(255),
                                     esignature_requested_at TIMESTAMP NULL,
                                     esignature_completed_at TIMESTAMP NULL,
                                     enrollment_agreement_accepted_at TIMESTAMP NULL,

                                     withdrawal_requested_at TIMESTAMP NULL,
                                     withdrawal_approved_at TIMESTAMP NULL,
                                     withdrawal_reason TEXT NULL,

                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(50),
                                     last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                     INDEX idx_user_id (user_id),
                                     INDEX idx_status (status),
                                     INDEX idx_created_at (created_at)
);

CREATE TABLE enrollment_history (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    user_id BIGINT,
                                    event VARCHAR(100) NOT NULL,
                                    event_date DATE NOT NULL,
                                    old_status VARCHAR(50),
                                    new_status VARCHAR(50),
                                    description TEXT,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    created_by VARCHAR(50),
                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                    INDEX idx_user_id (user_id),
                                    INDEX idx_event_date (event_date),
                                    INDEX idx_created_at (created_at)
);

-- ============================================================================
-- 6. ADMIN ACTIONS & VALIDATIONS
-- ============================================================================

CREATE TABLE validation_requests (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     validation_code VARCHAR(50) NOT NULL UNIQUE,
                                     event_type VARCHAR(100) NOT NULL,
                                     status ENUM('Pending', 'Validated', 'Rejected', 'Expired') DEFAULT 'Pending',
                                     emitter_admin_id BIGINT NOT NULL,
                                     required_role VARCHAR(100),
                                     required_approvals INT DEFAULT 1,
                                     import_id BIGINT NULL,

    -- Change Details
                                     change_details JSON NOT NULL,

    -- Approvals
                                     validation_notes JSON NULL,
                                     rejected_by BIGINT NULL,
                                     rejected_at TIMESTAMP NULL,
                                     rejection_reason TEXT NULL,

    -- Metadata
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     expires_at TIMESTAMP,
                                     completed_at TIMESTAMP NULL,

                                     FOREIGN KEY (emitter_admin_id) REFERENCES admins(id),
                                     FOREIGN KEY (rejected_by) REFERENCES admins(id),
                                     INDEX idx_status (status),
                                     INDEX idx_event_type (event_type),
                                     INDEX idx_created_at (created_at),
                                     INDEX idx_expires_at (expires_at)
);

CREATE TABLE validation_approvals (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      validation_id BIGINT NOT NULL,
                                      approver_admin_id BIGINT NOT NULL,
                                      approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      notes TEXT,
                                      FOREIGN KEY (validation_id) REFERENCES validation_requests(id) ON DELETE CASCADE,
                                      FOREIGN KEY (approver_admin_id) REFERENCES admins(id),
                                      UNIQUE KEY unique_validation_approver (validation_id, approver_admin_id),
                                      INDEX idx_validation_id (validation_id)
);

CREATE TABLE validation_rules (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  event_type VARCHAR(100) NOT NULL UNIQUE,
                                  required_role VARCHAR(100),
                                  required_approvals INT DEFAULT 1,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  created_by VARCHAR(50),
                                  last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================================
-- 7. DATA IMPORTS
-- ============================================================================

CREATE TABLE user_imports (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              import_code VARCHAR(50) NOT NULL UNIQUE,
                              file_name VARCHAR(255),
                              file_path VARCHAR(500),
                              file_size INT,
                              status ENUM('Pending', 'Processing', 'Validated', 'Rejected', 'Completed') DEFAULT 'Pending',
                              emitter_admin_id BIGINT NOT NULL,

    -- Statistics
                              total_records INT,
                              added_records INT DEFAULT 0,
                              modified_records INT DEFAULT 0,
                              missing_records INT DEFAULT 0,
                              failed_records INT DEFAULT 0,

    -- Import Details
                              import_data JSON,
                              missing_user_actions JSON NULL,

    -- Approvals
                              rejected_by BIGINT NULL,
                              rejected_at TIMESTAMP NULL,
                              rejection_reason TEXT NULL,

    -- Metadata
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              completed_at TIMESTAMP NULL,
                              FOREIGN KEY (emitter_admin_id) REFERENCES admins(id),
                              FOREIGN KEY (rejected_by) REFERENCES admins(id),
                              INDEX idx_status (status),
                              INDEX idx_created_at (created_at)
);

CREATE TABLE import_approvals (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  import_id BIGINT NOT NULL,
                                  import_type ENUM('User', 'Tax') NOT NULL,
                                  approver_admin_id BIGINT NOT NULL,
                                  approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  notes TEXT,
                                  FOREIGN KEY (import_id) REFERENCES user_imports(id) ON DELETE CASCADE,
                                  FOREIGN KEY (approver_admin_id) REFERENCES admins(id),
                                  UNIQUE KEY unique_import_approver (import_id, approver_admin_id),
                                  INDEX idx_import_id (import_id)
);

CREATE TABLE tax_imports (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             import_code VARCHAR(50) NOT NULL UNIQUE,
                             file_name VARCHAR(255),
                             file_path VARCHAR(500),
                             file_size INT,
                             status ENUM('Pending', 'Processing', 'Validated', 'Rejected', 'Completed') DEFAULT 'Pending',
                             emitter_admin_id BIGINT NOT NULL,

    -- Statistics
                             total_records INT,
                             added_records INT DEFAULT 0,
                             modified_records INT DEFAULT 0,
                             failed_records INT DEFAULT 0,

    -- Import Details
                             import_data JSON,

    -- Approvals
                             rejected_by BIGINT NULL,
                             rejected_at TIMESTAMP NULL,
                             rejection_reason TEXT NULL,

    -- Metadata
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             completed_at TIMESTAMP NULL,
                             FOREIGN KEY (emitter_admin_id) REFERENCES admins(id),
                             FOREIGN KEY (rejected_by) REFERENCES admins(id),
                             INDEX idx_status (status),
                             INDEX idx_created_at (created_at)
);

-- ============================================================================
-- 8. NEWS & ANNOUNCEMENTS
-- ============================================================================

CREATE TABLE news (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      news_code VARCHAR(50) NOT NULL UNIQUE,
                      title VARCHAR(255),
                      content TEXT NOT NULL,
                      start_date DATE NOT NULL,
                      end_date DATE NOT NULL,
                      is_active BOOLEAN DEFAULT TRUE,
                      display_order INT DEFAULT 0,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      created_by BIGINT,
                      last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      last_modified_by BIGINT,
                      FOREIGN KEY (created_by) REFERENCES admins(id),
                      FOREIGN KEY (last_modified_by) REFERENCES admins(id),
                      INDEX idx_active (is_active),
                      INDEX idx_start_date (start_date),
                      INDEX idx_end_date (end_date),
                      INDEX idx_display_order (display_order)
);

-- ============================================================================
-- 9. AUDIT & LOGGING
-- ============================================================================

CREATE TABLE audit_logs (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            action_type VARCHAR(100) NOT NULL,
                            entity_type VARCHAR(100),
                            entity_id BIGINT,
                            actor_id BIGINT,
                            actor_type ENUM('Admin', 'User', 'System') DEFAULT 'System',
                            old_values JSON,
                            new_values JSON,
                            ip_address VARCHAR(45),
                            user_agent TEXT,
                            status VARCHAR(50),
                            error_message TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_action_type (action_type),
                            INDEX idx_entity (entity_type, entity_id),
                            INDEX idx_actor (actor_id),
                            INDEX idx_created_at (created_at)
);

CREATE TABLE login_attempts (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                admin_id BIGINT,
                                email VARCHAR(255),
                                login_type ENUM('Admin', 'User') DEFAULT 'Admin',
                                status ENUM('Success', 'Failed', 'Locked') DEFAULT 'Failed',
                                failure_reason VARCHAR(255),
                                ip_address VARCHAR(45),
                                user_agent TEXT,
                                captcha_required BOOLEAN DEFAULT FALSE,
                                attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (admin_id) REFERENCES admins(id),
                                INDEX idx_email (email),
                                INDEX idx_admin_id (admin_id),
                                INDEX idx_attempted_at (attempted_at)
);

-- ============================================================================
-- 10. CHANGE LOGS
-- ============================================================================

CREATE TABLE unit_value_change_log (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       change_date DATE NOT NULL,
                                       old_value_eur DECIMAL(10, 2),
                                       new_value_eur DECIMAL(10, 2) NOT NULL,
                                       admin_id BIGINT,
                                       reason TEXT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (admin_id) REFERENCES admins(id),
                                       INDEX idx_change_date (change_date)
);

CREATE TABLE exchange_rate_change_log (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          change_date DATE NOT NULL,
                                          old_rate DECIMAL(15, 2),
                                          new_rate DECIMAL(15, 2) NOT NULL,
                                          source ENUM('Manual', 'API', 'Import') DEFAULT 'Manual',
                                          admin_id BIGINT,
                                          reason TEXT,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          FOREIGN KEY (admin_id) REFERENCES admins(id),
                                          INDEX idx_change_date (change_date)
);

CREATE TABLE salary_percentage_change_log (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              change_date DATE NOT NULL,
                                              old_percentage INT,
                                              new_percentage INT NOT NULL,
                                              admin_id BIGINT,
                                              reason TEXT,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              FOREIGN KEY (admin_id) REFERENCES admins(id),
                                              INDEX idx_change_date (change_date)
);

CREATE TABLE subscription_bonus_change_log (
                                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               change_date DATE NOT NULL,
                                               from_units DECIMAL(10, 2),
                                               to_units DECIMAL(10, 2),
                                               old_free_units INT,
                                               new_free_units INT,
                                               admin_id BIGINT,
                                               reason TEXT,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               FOREIGN KEY (admin_id) REFERENCES admins(id),
                                               INDEX idx_change_date (change_date)
);

CREATE TABLE free_unit_grant_history (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         grant_date DATE NOT NULL,
                                         user_id BIGINT,
                                         units_granted DECIMAL(10, 2),
                                         reason VARCHAR(255),
                                         admin_id BIGINT,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                         FOREIGN KEY (admin_id) REFERENCES admins(id),
                                         INDEX idx_user_id (user_id),
                                         INDEX idx_grant_date (grant_date)
);

-- ============================================================================
-- 11. REGISTRATION & PASSWORD MANAGEMENT
-- ============================================================================

CREATE TABLE registration_tokens (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     token VARCHAR(255) NOT NULL UNIQUE,
                                     user_fed_id VARCHAR(50) NOT NULL,
                                     user_email VARCHAR(255) NOT NULL,
                                     status ENUM('Active', 'Used', 'Expired', 'Revoked') DEFAULT 'Active',
                                     is_used BOOLEAN DEFAULT FALSE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     expires_at TIMESTAMP NOT NULL,
                                     used_at TIMESTAMP NULL,
                                     INDEX idx_token (token),
                                     INDEX idx_status (status),
                                     INDEX idx_expires_at (expires_at)
);

CREATE TABLE password_reset_tokens (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       token VARCHAR(255) NOT NULL UNIQUE,
                                       user_id BIGINT NOT NULL,
                                       token_type ENUM('Reset', 'Verification') DEFAULT 'Reset',
                                       status ENUM('Active', 'Used', 'Expired', 'Revoked') DEFAULT 'Active',
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       expires_at TIMESTAMP NOT NULL,
                                       used_at TIMESTAMP NULL,
                                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                       INDEX idx_token (token),
                                       INDEX idx_expires_at (expires_at)
);

-- ============================================================================
-- 12. SUPER ADMIN TRANSFER
-- ============================================================================

CREATE TABLE super_admin_transfers (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       transfer_code VARCHAR(50) NOT NULL UNIQUE,
                                       current_super_admin_id BIGINT NOT NULL,
                                       new_super_admin_id BIGINT NOT NULL,
                                       current_admin_new_role VARCHAR(100),
                                       status ENUM('Pending', 'Confirmed', 'Completed', 'Rejected', 'Expired') DEFAULT 'Pending',
                                       confirmation_token VARCHAR(255),
                                       confirmation_sent_at TIMESTAMP NULL,
                                       confirmation_expires_at TIMESTAMP NULL,
                                       confirmed_at TIMESTAMP NULL,
                                       completed_at TIMESTAMP NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (current_super_admin_id) REFERENCES admins(id),
                                       FOREIGN KEY (new_super_admin_id) REFERENCES admins(id),
                                       INDEX idx_status (status),
                                       INDEX idx_confirmation_expires_at (confirmation_expires_at)
);

-- ============================================================================
-- VIEWS FOR REPORTING
-- ============================================================================

CREATE VIEW v_active_users AS
SELECT
    u.id,
    u.fed_id,
    u.full_name,
    u.personal_email,
    d.name as department,
    u.job_title,
    l.name as location,
    u.employment_status,
    u.plan_participation_status,
    u.eligibility_status,
    u.created_at
FROM users u
         LEFT JOIN departments d ON u.department_id = d.id
         LEFT JOIN locations l ON u.location_id = l.id
WHERE u.employment_status = 'Active';

CREATE VIEW v_enrolled_users AS
SELECT
    u.id,
    u.fed_id,
    u.full_name,
    COUNT(t.id) as transaction_count,
    SUM(CASE WHEN t.status = 'Successful' THEN 1 ELSE 0 END) as successful_transactions,
    SUM(CASE WHEN t.status = 'Successful' THEN t.gross_amount ELSE 0 END) as total_invested
FROM users u
         LEFT JOIN transactions t ON u.id = t.user_id
WHERE u.plan_participation_status = 'Enrolled'
GROUP BY u.id, u.fed_id, u.full_name;

CREATE VIEW v_pending_validations AS
SELECT
    vr.id,
    vr.validation_code,
    vr.event_type,
    vr.status,
    a.full_name as emitter_name,
    COUNT(DISTINCT va.id) as approval_count,
    vr.required_approvals,
    vr.created_at
FROM validation_requests vr
         LEFT JOIN admins a ON vr.emitter_admin_id = a.id
         LEFT JOIN validation_approvals va ON vr.id = va.validation_id
WHERE vr.status = 'Pending'
GROUP BY vr.id;

CREATE VIEW v_pending_transactions AS
SELECT
    t.id,
    t.transaction_code,
    u.fed_id,
    u.full_name,
    t.type,
    t.status,
    t.gross_amount,
    t.transaction_date,
    t.created_at
FROM transactions t
         INNER JOIN users u ON t.user_id = u.id
WHERE t.status IN ('Submitted', 'Processing', 'Tax Outdated');

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_status_date ON transactions(status, transaction_date);
CREATE INDEX idx_users_department_location ON users(department_id, location_id);
CREATE INDEX idx_admins_role ON admins(role, is_active);
CREATE INDEX idx_validation_emitter_date ON validation_requests(emitter_admin_id, created_at);
CREATE INDEX idx_import_status_date ON user_imports(status, created_at);
CREATE INDEX idx_news_active_date ON news(is_active, start_date, end_date);
CREATE INDEX idx_audit_actor_date ON audit_logs(actor_id, created_at);