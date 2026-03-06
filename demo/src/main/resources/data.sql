-- Test Users (passwords are BCrypt encoded for "password123")
-- Password hash for "password123": $2a$10$SlYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Oy5ADGFVLP3nE9yK
INSERT INTO users (name, email, password, role)
VALUES ('John Doe', 'john@example.com', '$2a$10$SlYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Oy5ADGFVLP3nE9yK', 'USER');

INSERT INTO users (name, email, password, role)
VALUES ('Jane Smith', 'jane@example.com', '$2a$10$SlYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Oy5ADGFVLP3nE9yK', 'USER');

INSERT INTO users (name, email, password, role)
VALUES ('Admin User', 'admin@example.com', '$2a$10$SlYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Oy5ADGFVLP3nE9yK', 'ADMIN');

-- Admin Users (for legacy support)
INSERT INTO admin_user (username, password, role)
VALUES ('admin', 'admin123', 'ADMIN');

INSERT INTO admin_user (username, password, role)
VALUES ('hruser', 'hr123', 'HR');

INSERT INTO admin_user (username, password, role)
VALUES ('compliance', 'comp123', 'COMPLIANCE');

-- Sample Reports
INSERT INTO reports (token, title, category, severity, description, status, assigned_department, created_at)
VALUES ('token-001', 'Workplace Harassment Incident', 'Harassment', 'High', 'Experienced unwanted comments and gestures at workplace', 'Pending', 'HR', NOW());

INSERT INTO reports (token, title, category, severity, description, status, assigned_department, created_at)
VALUES ('token-002', 'Bullying by Manager', 'Bullying', 'Critical', 'Persistent bullying and inappropriate behavior from manager', 'In Review', 'HR', NOW());

INSERT INTO reports (token, title, category, severity, description, status, assigned_department, created_at)
VALUES ('token-003', 'Safety Protocol Violation', 'Workplace Safety', 'High', 'Safety equipment not being provided', 'Resolved', 'Compliance', NOW());

INSERT INTO reports (token, title, category, severity, description, status, assigned_department, created_at)
VALUES ('token-004', 'Discrimination Issue', 'Discrimination', 'Critical', 'Discriminatory treatment based on age and ethnicity', 'Pending', 'HR', NOW());

INSERT INTO reports (token, title, category, severity, description, status, assigned_department, created_at)
VALUES ('token-005', 'Ethical Concern', 'Ethical Violation', 'Medium', 'Potential conflict of interest in procurement process', 'In Review', 'Compliance', NOW());

-- Sample Audit Logs
INSERT INTO audit_log (username, role, action, details, timestamp)
VALUES ('admin@example.com', 'ADMIN', 'LOGIN', 'Admin user logged in', NOW());

INSERT INTO audit_log (username, role, action, details, timestamp)
VALUES ('admin@example.com', 'ADMIN', 'VIEW_ALL_REPORTS', 'Admin viewed all reports', NOW());

INSERT INTO audit_log (username, role, action, details, timestamp)
VALUES ('admin@example.com', 'ADMIN', 'UPDATE_REPORT_STATUS', 'Updated report token-002 status to In Review', NOW());

-- Sample Notifications
INSERT INTO notification (title, message, type, related_report_id, is_read, created_at)
VALUES ('New Report Submitted', 'A new report has been submitted with severity: High', 'REPORT_CREATED', 1, false, NOW());

INSERT INTO notification (title, message, type, related_report_id, is_read, created_at)
VALUES ('Report Status Updated', 'Report status has been changed to: In Review', 'REPORT_STATUS_UPDATED', 2, false, NOW());

INSERT INTO notification (title, message, type, related_report_id, is_read, created_at)
VALUES ('Admin Response Added', 'An admin response has been added to your report', 'ADMIN_RESPONSE_ADDED', 3, true, NOW());