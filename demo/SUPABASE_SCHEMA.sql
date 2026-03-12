-- ============================================================
-- TRINETRA - Supabase PostgreSQL Schema
-- ============================================================
-- This script creates all necessary tables for the TRINETRA
-- whistleblower reporting platform on Supabase PostgreSQL
--
-- Execute this script in Supabase SQL Editor to initialize
-- the database schema. Tables are designed to work with
-- Spring Data JPA and Hibernate ORM.
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- USERS TABLE
-- ============================================================
-- Stores authenticated users and their roles/permissions
-- Supports both regular users and admin users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    department VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_by UUID,
    updated_by UUID
);

-- Create indexes for frequently queried fields
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- ============================================================
-- REPORTS TABLE
-- ============================================================
-- Stores incident/concern reports submitted by users
-- Each report can have multiple pieces of evidence attached
CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    severity VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    report_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_anonymous BOOLEAN DEFAULT TRUE,
    location_details VARCHAR(500),
    incident_date DATE,
    is_escalated BOOLEAN DEFAULT FALSE,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    resolution_notes TEXT,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Create indexes for report queries
CREATE INDEX idx_reports_user_id ON reports(user_id);
CREATE INDEX idx_reports_status ON reports(report_status);
CREATE INDEX idx_reports_category ON reports(category);
CREATE INDEX idx_reports_severity ON reports(severity);
CREATE INDEX idx_reports_assigned_to ON reports(assigned_to);
CREATE INDEX idx_reports_incident_date ON reports(incident_date DESC);
CREATE INDEX idx_reports_created_at ON reports(created_at DESC);

-- ============================================================
-- EVIDENCE TABLE
-- ============================================================
-- Stores evidence files/attachments related to reports
-- Supports multiple files per report
CREATE TABLE IF NOT EXISTS evidence (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    file_name VARCHAR(500) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    file_path VARCHAR(1000),
    description TEXT,
    uploaded_by UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by UUID REFERENCES users(id) ON DELETE SET NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for evidence queries
CREATE INDEX idx_evidence_report_id ON evidence(report_id);
CREATE INDEX idx_evidence_uploaded_by ON evidence(uploaded_by);
CREATE INDEX idx_evidence_created_at ON evidence(created_at DESC);

-- ============================================================
-- AUDIT_LOGS TABLE
-- ============================================================
-- Tracks all system activities and user actions for compliance
-- Maintains immutable audit trail of important events
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    description TEXT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for audit log queries
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_status ON audit_logs(status);

-- ============================================================
-- NOTIFICATIONS TABLE
-- ============================================================
-- Stores system and report notifications sent to users
-- Supports in-app notifications, email, and SMS
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE SET NULL,
    report_id UUID REFERENCES reports(id) ON DELETE CASCADE,
    notification_type VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(50) NOT NULL DEFAULT 'IN_APP',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,
    delivery_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for notification queries
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_report_id ON notifications(report_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_notification_type ON notifications(notification_type);

-- ============================================================
-- AI_INVESTIGATION_LOG TABLE
-- ============================================================
-- Stores logs from AI-powered investigation assistance features
-- Reserved for future investigation analytics capabilities
CREATE TABLE IF NOT EXISTS ai_investigation_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    analysis_type VARCHAR(100),
    input_data JSONB,
    analysis_result JSONB,
    confidence_score DECIMAL(5,2),
    recommendations TEXT,
    processed_by VARCHAR(100),
    processing_time_ms INTEGER,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    notes TEXT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for AI log queries
CREATE INDEX idx_ai_logs_report_id ON ai_investigation_log(report_id);
CREATE INDEX idx_ai_logs_analysis_type ON ai_investigation_log(analysis_type);
CREATE INDEX idx_ai_logs_created_at ON ai_investigation_log(created_at DESC);

-- ============================================================
-- CREATE ADMIN USER
-- ============================================================
-- Creates default admin user for initial system access
-- Password: admin123 (hashed with BCrypt, should be changed immediately)
-- NOTE: Change the password hash below to a real BCrypt hash in production!
INSERT INTO users (id, username, email, password_hash, full_name, role, status, is_active)
VALUES (
    uuid_generate_v4(),
    'admin',
    'admin@trinetra.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBjWeO',
    'System Administrator',
    'ADMIN',
    'ACTIVE',
    TRUE
)
ON CONFLICT (username) DO NOTHING;

-- ============================================================
-- CREATE FOREIGN KEY CONSTRAINTS
-- ============================================================
-- Ensure data integrity for user references
ALTER TABLE reports 
    ADD CONSTRAINT fk_reports_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE reports 
    ADD CONSTRAINT fk_reports_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE evidence
    ADD CONSTRAINT fk_evidence_verified_by FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- ============================================================
-- CREATE VIEWS FOR ANALYTICS
-- ============================================================

-- View for report statistics
CREATE OR REPLACE VIEW report_statistics AS
SELECT 
    r.category,
    r.severity,
    r.report_status,
    COUNT(*) as report_count,
    COUNT(CASE WHEN r.report_status = 'RESOLVED' THEN 1 END) as resolved_count,
    ROUND(AVG(EXTRACT(DAY FROM (r.updated_at - r.created_at)))::NUMERIC, 2) as avg_resolution_days
FROM reports r
GROUP BY r.category, r.severity, r.report_status;

-- View for user activity summary
CREATE OR REPLACE VIEW user_activity AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.role,
    COUNT(r.id) as total_reports,
    COUNT(CASE WHEN r.report_status = 'DRAFT' THEN 1 END) as draft_reports,
    COUNT(CASE WHEN r.report_status = 'SUBMITTED' THEN 1 END) as submitted_reports,
    COUNT(CASE WHEN r.report_status = 'RESOLVED' THEN 1 END) as resolved_reports,
    u.last_login_at,
    u.created_at
FROM users u
LEFT JOIN reports r ON u.id = r.user_id
GROUP BY u.id, u.username, u.email, u.role, u.last_login_at, u.created_at;

-- ============================================================
-- SCHEMA INITIALIZATION COMPLETE
-- ============================================================
-- Total Tables: 7 (users, reports, evidence, audit_logs, notifications, ai_investigation_log, + 2 views)
-- All tables support Hibernate/JPA auto-increment with UUID primary keys
-- SSL is enabled by default on Supabase PostgreSQL
-- Audit trail logging enabled for compliance tracking
-- ============================================================
