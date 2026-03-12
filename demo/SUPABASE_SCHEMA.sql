-- ============================================================
-- TRINETRA - PostgreSQL Schema for Spring Boot Entity Models
-- ============================================================
-- This script creates tables matching the actual JPA entity
-- definitions in the TRINETRA application.
-- All tables use BIGINT with auto-increment (IDENTITY)
-- ============================================================

-- ============================================================
-- USERS TABLE
-- ============================================================
-- Stores authenticated users (matches User.java entity)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER'
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- ============================================================
-- REPORTS TABLE
-- ============================================================
-- Stores incident reports (matches Report.java entity)
CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    category VARCHAR(100) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(100),
    admin_response TEXT,
    assigned_department VARCHAR(255),
    created_at TIMESTAMP
);

CREATE INDEX idx_reports_token ON reports(token);
CREATE INDEX idx_reports_category ON reports(category);
CREATE INDEX idx_reports_status ON reports(status);

-- ============================================================
-- AUDIT_LOG TABLE
-- ============================================================
-- Stores audit trail (matches AuditLog.java entity)
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255),
    username VARCHAR(255),
    role VARCHAR(50),
    details TEXT,
    timestamp TIMESTAMP
);

CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_username ON audit_log(username);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp DESC);

-- ============================================================
-- NOTIFICATION TABLE
-- ============================================================
-- Stores notifications (matches Notification.java entity)
CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    message TEXT,
    type VARCHAR(100),
    related_report_id BIGINT,
    created_at TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_notification_related_report_id ON notification(related_report_id);
CREATE INDEX idx_notification_created_at ON notification(created_at DESC);

-- ============================================================
-- SCHEMA INITIALIZATION COMPLETE
-- ============================================================
-- Tables created match Spring Data JPA entity definitions
-- All IDs use BIGINT with auto-increment (IDENTITY strategy)
-- No UUID foreign keys - simple BIGINT references
-- ============================================================
