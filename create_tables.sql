-- Authentication database tables
CREATE TABLE authentication (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_id BIGINT,
    token VARCHAR(255),
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP,
    mfa_enabled BOOLEAN DEFAULT false,
    mfa_secret VARCHAR(255),
    failed_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT REFERENCES authentication(id),
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE oauth_providers (
    user_id BIGINT REFERENCES authentication(id),
    provider_name VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, provider_name)
);

-- OTP database tables
CREATE TABLE otps (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    recipient_type VARCHAR(50) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    used BOOLEAN DEFAULT false,
    purpose VARCHAR(50) NOT NULL,
    retry_count INTEGER DEFAULT 0,
    user_id BIGINT,
    token VARCHAR(255),
    max_retries INTEGER DEFAULT 3,
    verified BOOLEAN DEFAULT false,
    attempts INTEGER DEFAULT 0
);

-- User database tables
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    image_path VARCHAR(255),
    member_type VARCHAR(50) NOT NULL,
    is_verified BOOLEAN DEFAULT false,
    address TEXT,
    mobile_number VARCHAR(20),
    role VARCHAR(50),
    category VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP
);

CREATE TABLE preferred_categories (
    user_id BIGINT REFERENCES users(id),
    category VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, category)
);

-- Mess database tables
CREATE TABLE mess_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    profile_image_url VARCHAR(255),
    last_login_at TIMESTAMP,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT true,
    enabled BOOLEAN DEFAULT false,
    email_verified BOOLEAN DEFAULT false,
    locked BOOLEAN DEFAULT false,
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
); 