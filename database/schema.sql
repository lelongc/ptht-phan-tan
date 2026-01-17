-- ===================================================
-- CREATE DATABASE & SET CHARSET
-- ===================================================
DROP DATABASE IF EXISTS ebook_store;
CREATE DATABASE ebook_store CHARACTER SET utf8 COLLATE utf8_unicode_ci;
USE ebook_store;

-- ===================================================
-- EBOOK STORE - SIMPLIFIED SCHEMA (1 ebook only)
-- ===================================================

-- ============ USERS TABLE ============
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255),
  phone VARCHAR(50),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  INDEX idx_user_email (email),
  INDEX idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ============ EBOOK TABLE (1 ebook, multi-lang) ============
CREATE TABLE IF NOT EXISTS ebooks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  -- Vietnamese content
  title_vi VARCHAR(255) NOT NULL,
  author_vi VARCHAR(255),
  description_vi LONGTEXT,
  
  -- English content
  title_en VARCHAR(255) NOT NULL,
  author_en VARCHAR(255),
  description_en LONGTEXT,
  
  -- File & Metadata
  price DECIMAL(10,2) NOT NULL,
  cover_url VARCHAR(500),
  s3_key VARCHAR(500),
  file_size_mb DECIMAL(10,2),
  page_count INT,
  
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  INDEX idx_ebook_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ============ ORDERS TABLE ============
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  -- Relationships
  user_id BIGINT NOT NULL,
  ebook_id BIGINT NOT NULL,
  
  -- Payment info
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, PAID, FAILED, EXPIRED
  payment_method VARCHAR(20),  -- PAYPAL, VIETQR
  transaction_id VARCHAR(255),
  
  -- Security
  secret_code VARCHAR(64) NOT NULL UNIQUE,  -- For polling payment status
  
  -- Payer info
  payer_email VARCHAR(255),
  payer_name VARCHAR(255),
  
  -- Timestamps
  paid_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,  -- Order expires after 15 minutes
  download_expires_at TIMESTAMP NULL,  -- Download link valid 24h after payment
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (ebook_id) REFERENCES ebooks(id),
  
  INDEX idx_order_status (status),
  INDEX idx_order_secret (secret_code),
  INDEX idx_order_user (user_id),
  INDEX idx_order_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ============ DOWNLOAD TOKENS TABLE ============
CREATE TABLE IF NOT EXISTS download_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  order_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,  -- JWT token
  
  expires_at TIMESTAMP NOT NULL,
  used_count INT DEFAULT 0,
  max_uses INT DEFAULT 5,  -- Max 5 download attempts
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  
  INDEX idx_token (token),
  INDEX idx_token_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ============ WEBHOOK LOGS TABLE (for debugging) ============
CREATE TABLE IF NOT EXISTS webhook_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  provider VARCHAR(50),  -- PAYPAL, VIETQR
  order_id BIGINT,
  
  -- Raw webhook data
  payload LONGTEXT,
  status_code INT,
  response_message VARCHAR(500),
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_webhook_provider (provider),
  INDEX idx_webhook_order (order_id),
  INDEX idx_webhook_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
