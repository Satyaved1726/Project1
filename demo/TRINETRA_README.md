# TRINETRA - Anonymous Workplace Reporting System

## Overview

TRINETRA is an enterprise-grade, anonymous workplace reporting system built with Java 21, Spring Boot, Spring Security, and MySQL. It provides a secure platform for employees to submit anonymous reports about workplace issues while enabling administrators to manage, respond to, and track these incidents.

## Technology Stack

- **Java Version**: 21 (LTS)
- **Framework**: Spring Boot 4.0.3
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT Authentication
- **Database**: MySQL 8.0+
- **Build Tool**: Maven
- **Authentication**: JSON Web Token (JWT) with JJWT library

## Prerequisites

- Java JDK 21 installed
- MySQL Server 8.0 or higher
- Maven 3.8.1 or higher
- Git

## Installation & Setup

### 1. Database Setup

Create a MySQL database for TRINETRA:

```sql
CREATE DATABASE trinetra;
CREATE USER 'trinetra_user'@'localhost' IDENTIFIED BY 'trinetra_password';
GRANT ALL PRIVILEGES ON trinetra.* TO 'trinetra_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Application Configuration

Update `application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/trinetra?useSSL=false&serverTimezone=UTC
spring.datasource.username=trinetra_user
spring.datasource.password=trinetra_password
```

### 3. Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## Database Schema

### Users Table
- **id** (PK, BIGINT, AUTO_INCREMENT)
- **name** (VARCHAR(255), NOT NULL)
- **email** (VARCHAR(255), UNIQUE, NOT NULL)
- **password** (VARCHAR(255), NOT NULL - BCrypt Encoded)
- **role** (VARCHAR(50), NOT NULL - USER, ADMIN)

### Reports Table
- **id** (PK, BIGINT, AUTO_INCREMENT)
- **token** (VARCHAR(255), UNIQUE, NOT NULL)
- **title** (VARCHAR(255), NOT NULL)
- **category** (VARCHAR(100), NOT NULL)
- **severity** (VARCHAR(50), NOT NULL - Low, Medium, High, Critical)
- **description** (TEXT)
- **status** (VARCHAR(50), NOT NULL - Pending, In Review, Resolved)
- **admin_response** (TEXT)
- **assigned_department** (VARCHAR(100))
- **created_at** (DATETIME, NOT NULL)

### AdminUser Table
- **id** (PK, BIGINT, AUTO_INCREMENT)
- **username** (VARCHAR(255), UNIQUE, NOT NULL)
- **password** (VARCHAR(255), NOT NULL)
- **role** (VARCHAR(50), NOT NULL)

### AuditLog Table
- **id** (PK, BIGINT, AUTO_INCREMENT)
- **username** (VARCHAR(255))
- **role** (VARCHAR(50))
- **action** (VARCHAR(100))
- **details** (TEXT)
- **timestamp** (DATETIME)

### Notification Table
- **id** (PK, BIGINT, AUTO_INCREMENT)
- **title** (VARCHAR(255))
- **message** (TEXT)
- **type** (VARCHAR(100))
- **related_report_id** (BIGINT)
- **is_read** (BOOLEAN)
- **created_at** (DATETIME)

## API Endpoints

### Authentication Endpoints

#### User Signup
- **POST** `/api/auth/signup`
- **Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```
- **Response**: JWT Token

#### User Login
- **POST** `/api/auth/login`
- **Request Body**:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
- **Response**: JWT Token

### Report Endpoints (Public)

#### Submit Anonymous Report
- **POST** `/api/reports/submit`
- **Authentication**: None (Public)
- **Request Body**:
```json
{
  "title": "Workplace Harassment",
  "category": "Harassment",
  "severity": "High",
  "description": "Detailed description of the incident..."
}
```
- **Response**: 
```json
{
  "success": true,
  "message": "Report submitted successfully",
  "data": {
    "id": 1,
    "token": "unique-tracking-token",
    "title": "Workplace Harassment",
    ...
  }
}
```

#### Track Report by Token
- **GET** `/api/reports/token/{token}`
- **Authentication**: None (Public)
- **Response**: Report details

### Administrator Endpoints (Require ADMIN Role)

#### List All Reports
- **GET** `/api/admin/reports`
- **Authentication**: Required (ADMIN role)
- **Response**: List of all reports

#### Update Report Status
- **PUT** `/api/admin/update-status/{id}`
- **Authentication**: Required (ADMIN role)
- **Request Body**:
```json
{
  "status": "In Review"
}
```
- **Valid Statuses**: Pending, In Review, Resolved

#### Add Admin Response
- **POST** `/api/admin/respond/{id}`
- **Authentication**: Required (ADMIN role)
- **Request Body**:
```json
{
  "response": "We have investigated this matter and taken appropriate action..."
}
```

#### Assign Department
- **PUT** `/api/admin/assign-department/{id}`
- **Authentication**: Required (ADMIN role)
- **Request Body**:
```json
{
  "department": "HR"
}
```

#### Get Analytics
- **GET** `/api/admin/analytics`
- **Authentication**: Required (ADMIN role)
- **Response**:
```json
{
  "success": true,
  "message": "Analytics retrieved",
  "data": {
    "totalReports": 150,
    "reportsByCategory": {
      "Harassment": 45,
      "Bullying": 30,
      "Discrimination": 25,
      ...
    },
    "reportsBySeverity": {
      "Low": 20,
      "Medium": 50,
      "High": 60,
      "Critical": 20
    },
    "reportsByStatus": {
      "Pending": 30,
      "In Review": 50,
      "Resolved": 70
    }
  }
}
```

### Notification Endpoints

#### Get Unread Notifications
- **GET** `/api/notifications/unread`
- **Authentication**: Required

#### Get Unread Count
- **GET** `/api/notifications/unread/count`
- **Authentication**: Required

#### Get Notifications by Report
- **GET** `/api/notifications/report/{reportId}`
- **Authentication**: Required

#### Mark as Read
- **PUT** `/api/notifications/{id}/read`
- **Authentication**: Required

#### Delete Notification
- **DELETE** `/api/notifications/{id}`
- **Authentication**: Required

### Audit Log Endpoints (Admin Only)

#### Get All Audit Logs
- **GET** `/api/audit-logs`
- **Authentication**: Required (ADMIN role)

#### Get Logs by Username
- **GET** `/api/audit-logs/user/{username}`
- **Authentication**: Required (ADMIN role)

#### Get Logs by Action
- **GET** `/api/audit-logs/action/{action}`
- **Authentication**: Required (ADMIN role)

#### Get Logs by Time Range
- **GET** `/api/audit-logs/range?start=yyyy-MM-ddThh:mm:ss&end=yyyy-MM-ddThh:mm:ss`
- **Authentication**: Required (ADMIN role)

## Security Features

### JWT Authentication
- All protected endpoints require a valid JWT token in the `Authorization` header
- Token format: `Bearer <token>`
- Token expiration: Configurable (default: 24 hours)

### Password Encryption
- All passwords are BCrypt encoded
- Never stored in plain text

### Role-Based Access Control (RBAC)
- **USER**: Can submit reports and view own notifications
- **ADMIN**: Can manage all reports, view analytics, and access admin endpoints

### CORS Configuration
- Configured for local development and production domains
- Defaults: `http://localhost:3000`, `http://localhost:4200`

### Audit Logging
- All admin actions are logged
- Tracks who did what and when
- Useful for compliance and security monitoring

## Project Structure

```
src/main/java/com/safevoice/
├── config/
│   ├── CorsConfig.java
│   ├── JpaConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── ReportController.java
│   ├── AdminReportController.java
│   ├── NotificationController.java
│   ├── AuditLogController.java
│   ├── HealthController.java
│   └── GlobalExceptionHandler.java
├── dto/
│   ├── UserSignupRequest.java
│   ├── UserLoginRequest.java
│   ├── ReportRequest.java
│   ├── ReportResponse.java
│   ├── AuthResponse.java
│   ├── AnalyticsResponse.java
│   ├── StatusUpdateRequest.java
│   ├── AdminResponseRequest.java
│   ├── DepartmentAssignmentRequest.java
│   └── ApiResponse.java
├── exception/
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
├── model/
│   ├── User.java
│   ├── Report.java
│   ├── AdminUser.java
│   ├── AuditLog.java
│   └── Notification.java
├── repository/
│   ├── UserRepository.java
│   ├── ReportRepository.java
│   ├── AdminUserRepository.java
│   ├── AuditLogRepository.java
│   └── NotificationRepository.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── ReportService.java
    ├── NotificationService.java
    ├── AuditLogService.java
    ├── DepartmentRoutingService.java
    ├── SeverityAnalyzerService.java
    └── RateLimitService.java
```

## Default Test Credentials

### Regular Users
- Email: `john@example.com`, Password: `password123`
- Email: `jane@example.com`, Password: `password123`

### Admin User
- Email: `admin@example.com`, Password: `password123`

## Rate Limiting

- Anonymous report submission: 5 requests per minute per IP address
- Helps prevent spam and abuse

## Error Handling

The API returns standardized error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

HTTP Status Codes:
- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `500 Internal Server Error`: Server error

## Best Practices for Production

1. **Environment Variables**: Store sensitive values in environment variables
   - `spring.datasource.password`
   - `jwt.secret`
   - `app.cors.allowed-origins`

2. **HTTPS**: Always use HTTPS in production
   - Configure SSL/TLS certificates

3. **Database Backups**: Implement regular automated backups

4. **Monitoring**: Set up monitoring and alerting
   - Application health checks
   - Database performance
   - Security events

5. **Logging**: Configure centralized logging
   - Use log aggregation tools
   - Monitor for suspicious activities

6. **Security Headers**: Add security headers
   - Content-Security-Policy
   - X-Frame-Options
   - X-Content-Type-Options

7. **Rate Limiting**: Implement rate limiting for all endpoints

8. **JWT Refresh Tokens**: Implement refresh token mechanism for long-lived sessions

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running
- Check database credentials in `application.properties`
- Ensure database exists and is accessible

### JWT Token Errors
- Ensure JWT secret is properly configured
- Check token format: `Authorization: Bearer <token>`
- Verify token hasn't expired

### Port Already in Use
- Change `server.port` in `application.properties`
- Or kill the process using port 8081

## Support & Documentation

For more information:
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Documentation](https://github.com/jwtk/jjwt)

## License

This project is proprietary and confidential.

## Contact

For support or inquiries, contact the development team.
