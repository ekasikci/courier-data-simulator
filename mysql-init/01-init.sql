-- Create database if not exists
CREATE DATABASE IF NOT EXISTS package_db;

USE package_db;

-- Create packages table
CREATE TABLE IF NOT EXISTS packages (
    id BIGINT PRIMARY KEY,
    arrival_for_delivery_at DATETIME(6),
    arrival_for_pickup_at DATETIME(6),
    cancel_reason VARCHAR(255),
    cancelled INT DEFAULT 0,
    completed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    customer_id BIGINT,
    in_delivery_at DATETIME(6),
    last_updated_at DATETIME(6),
    eta INT,
    status VARCHAR(50),
    store_id BIGINT,
    origin_address_id BIGINT,
    type VARCHAR(50),
    waiting_for_assignment_at DATETIME(6),
    user_id BIGINT,
    collected INT DEFAULT 0,
    collected_at DATETIME(6),
    cancelled_at DATETIME(6),
    picked_up_at DATETIME(6),
    reassigned INT,
    order_id BIGINT,
    delivery_date VARCHAR(255),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status),
    INDEX idx_cancelled (cancelled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- Grant necessary permissions for replication (Debezium needs these)
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'dbuser'@'%';
FLUSH PRIVILEGES;