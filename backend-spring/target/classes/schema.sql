-- schema.sql (MySQL 8.0)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(100) UNIQUE,
    name VARCHAR(255) NOT NULL,
    active_ingredient VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    formulation VARCHAR(100),
    dose VARCHAR(100),
    targets TEXT,
    crops TEXT,
    application_interval VARCHAR(255),
    price VARCHAR(100),
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS diagnoses (
    id VARCHAR(64) PRIMARY KEY,
    client_name VARCHAR(150),
    client_email VARCHAR(150),
    image_url VARCHAR(500),
    organ VARCHAR(50),
    plant_species VARCHAR(200),
    plant_common_name VARCHAR(200),
    plant_confidence DOUBLE,
    disease_name VARCHAR(250),
    eppo_code VARCHAR(50),
    disease_confidence DOUBLE,
    urgency VARCHAR(50),
    follow_up_days INT,
    source VARCHAR(50),
    recommendation TEXT,
    needs_expert BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quote_requests (
    id VARCHAR(64) PRIMARY KEY,
    diagnosis_id VARCHAR(64),
    client_name VARCHAR(150),
    client_email VARCHAR(150),
    client_phone VARCHAR(50),
    status VARCHAR(50) DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quote_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES diagnoses(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quote_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quote_request_id VARCHAR(64) NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(255) NOT NULL,
    dose VARCHAR(100),
    price VARCHAR(100),
    CONSTRAINT fk_item_quote FOREIGN KEY (quote_request_id) REFERENCES quote_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
