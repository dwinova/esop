CREATE TABLE members
(
    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) DEFAULT NULL,
    password VARCHAR(255) DEFAULT NULL,
    role VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE member_profiles
(
    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(1000) DEFAULT NULL,
    gender VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE INDEX idx_user_email ON members(email);
