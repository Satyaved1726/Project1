# TRINETRA API Testing Guide

This guide provides curl commands and instructions for testing all API endpoints of the TRINETRA system.

## Base URL
```
http://localhost:8081
```

## Setup

### Start the Application
```bash
cd demo
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### Test Database
The `data.sql` file is automatically executed on startup, creating test data.

---

## 1. Authentication Endpoints

### 1.1 User Signup

```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New User",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "newuser@example.com",
    "role": "USER"
  }
}
```

### 1.2 User Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "john@example.com",
    "role": "USER"
  }
}
```

Save the token for authenticated requests.

---

## 2. Report Submission (Anonymous - Public Endpoints)

### 2.1 Submit Anonymous Report

```bash
curl -X POST http://localhost:8081/api/reports/submit \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Workplace Harassment Incident",
    "category": "Harassment",
    "severity": "High",
    "description": "I experienced inappropriate comments from my supervisor in the workplace."
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Report submitted successfully",
  "data": {
    "id": 6,
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Workplace Harassment Incident",
    "category": "Harassment",
    "severity": "High",
    "description": "...",
    "status": "Pending",
    "createdAt": "2024-03-06T10:30:00"
  }
}
```

**Important**: Save the `token` to track this report later.

### 2.2 Track Report by Token

```bash
curl -X GET "http://localhost:8081/api/reports/token/550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Report found",
  "data": {
    "id": 6,
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Workplace Harassment Incident",
    "category": "Harassment",
    "severity": "High",
    "status": "Pending",
    "adminResponse": null,
    "createdAt": "2024-03-06T10:30:00"
  }
}
```

---

## 3. Admin Endpoints (Require Authentication & ADMIN Role)

### Getting Admin Token

First, login with admin credentials:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password123"
  }'
```

Extract the `token` from the response and use it for all admin requests:
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."
```

### 3.1 Get All Reports

```bash
curl -X GET http://localhost:8081/api/admin/reports \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Reports retrieved",
  "data": [
    {
      "id": 1,
      "token": "token-001",
      "title": "Workplace Harassment Incident",
      "category": "Harassment",
      "severity": "High",
      "status": "Pending",
      "assignedDepartment": "HR",
      "createdAt": "2024-03-06T08:00:00"
    },
    ...
  ]
}
```

### 3.2 Update Report Status

```bash
curl -X PUT http://localhost:8081/api/admin/update-status/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "In Review"
  }'
```

**Valid Statuses**: `Pending`, `In Review`, `Resolved`

**Expected Response:**
```json
{
  "success": true,
  "message": "Report status updated successfully",
  "data": {
    "id": 1,
    "status": "In Review",
    ...
  }
}
```

### 3.3 Add Admin Response to Report

```bash
curl -X POST http://localhost:8081/api/admin/respond/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "response": "Thank you for reporting this issue. We have investigated the matter and taken appropriate disciplinary action. The situation will be monitored closely."
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Admin response added successfully",
  "data": {
    "id": 1,
    "adminResponse": "Thank you for reporting this issue...",
    ...
  }
}
```

### 3.4 Assign Department to Report

```bash
curl -X PUT http://localhost:8081/api/admin/assign-department/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "department": "Compliance"
  }'
```

**Expected Departments**: `HR`, `Compliance`, `Admin`

**Expected Response:**
```json
{
  "success": true,
  "message": "Department assigned successfully",
  "data": {
    "id": 1,
    "assignedDepartment": "Compliance",
    ...
  }
}
```

### 3.5 Get Analytics

```bash
curl -X GET http://localhost:8081/api/admin/analytics \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Analytics retrieved",
  "data": {
    "totalReports": 5,
    "reportsByCategory": {
      "Harassment": 2,
      "Bullying": 1,
      "Discrimination": 1,
      "Ethical Violation": 1
    },
    "reportsBySeverity": {
      "Low": 0,
      "Medium": 1,
      "High": 3,
      "Critical": 1
    },
    "reportsByStatus": {
      "Pending": 1,
      "In Review": 2,
      "Resolved": 2
    }
  }
}
```

---

## 4. Notification Endpoints

### 4.1 Get Unread Notifications

```bash
curl -X GET http://localhost:8081/api/notifications/unread \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### 4.2 Get Unread Count

```bash
curl -X GET http://localhost:8081/api/notifications/unread/count \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Unread count",
  "data": 3
}
```

### 4.3 Get Notifications by Report

```bash
curl -X GET http://localhost:8081/api/notifications/report/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### 4.4 Mark Notification as Read

```bash
curl -X PUT http://localhost:8081/api/notifications/1/read \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### 4.5 Delete Notification

```bash
curl -X DELETE http://localhost:8081/api/notifications/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## 5. Audit Log Endpoints (Admin Only)

### 5.1 Get All Audit Logs

```bash
curl -X GET http://localhost:8081/api/audit-logs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### 5.2 Get Logs by Username

```bash
curl -X GET http://localhost:8081/api/audit-logs/user/admin@example.com \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### 5.3 Get Logs by Action

```bash
curl -X GET http://localhost:8081/api/audit-logs/action/LOGIN \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Valid Actions**: `LOGIN`, `LOGOUT`, `VIEW_REPORTS`, `UPDATE_REPORT_STATUS`, `ADD_ADMIN_RESPONSE`, etc.

### 5.4 Get Logs by Time Range

```bash
curl -X GET "http://localhost:8081/api/audit-logs/range?start=2024-03-01T00:00:00&end=2024-03-07T23:59:59" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## 6. Health Check Endpoint

### Check Application Health

```bash
curl -X GET http://localhost:8081/api/health \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "status": "UP",
  "service": "TRINETRA Backend",
  "database": "Connected",
  "timestamp": "2024-03-06T10:30:00"
}
```

---

## Testing with Postman

1. **Import the API Collection**:
   - Create a new Postman collection
   - Add all endpoints listed above

2. **Set Environment Variable**:
   - Create an environment variable `token`
   - After login, update the variable with the received token

3. **Use Pre-request Scripts**:
   - Automatically get a token before running admin endpoints

---

## Error Responses

### 401 Unauthorized (Missing Token)
```json
{
  "success": false,
  "message": "Unauthorized access",
  "data": null
}
```

### 403 Forbidden (Insufficient Permissions)
```json
{
  "success": false,
  "message": "Access Denied",
  "data": null
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Report not found with id: 999",
  "data": null
}
```

### 400 Bad Request (Validation Error)
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  }
}
```

---

## Rate Limiting

Anonymous report submission is rate-limited to **5 requests per minute** per IP address.

If exceeded:
```
HTTP Status: 429 Too Many Requests
```

---

## Tips for Testing

1. **Use a REST Client**: Postman, Insomnia, or VS Code REST Client
2. **Keep Tokens Handy**: Save tokens for quick testing
3. **Test Negative Cases**: Invalid emails, wrong passwords, etc.
4. **Check Database**: Verify data is saved in MySQL after each request
5. **Monitor Logs**: Check application logs for debugging

---

## Troubleshooting

**Issue**: 401 Unauthorized
- **Solution**: Ensure token is valid and not expired. Get a fresh token by logging in again.

**Issue**: 403 Forbidden
- **Solution**: Ensure you're using an admin account token for admin endpoints.

**Issue**: 404 Not Found
- **Solution**: Check the report ID or token exists in the database.

**Issue**: 429 Too Many Requests
- **Solution**: Wait a minute before submitting another anonymous report from the same IP.

---

For more help, refer to `TRINETRA_README.md`
