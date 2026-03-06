# TRINETRA Backend - Render Deployment Guide

## PostgreSQL Migration & Render Configuration

This guide provides step-by-step instructions for deploying the TRINETRA backend to **Render** cloud hosting with **PostgreSQL** database.

---

## Table of Contents

1. [Overview of Changes](#overview-of-changes)
2. [Prerequisites](#prerequisites)
3. [Step 1: Build the Application](#step-1-build-the-application)
4. [Step 2: Prepare for Render](#step-2-prepare-for-render)
5. [Step 3: Create Render Resources](#step-3-create-render-resources)
6. [Step 4: Configure Environment Variables](#step-4-configure-environment-variables)
7. [Step 5: Deploy to Render](#step-5-deploy-to-render)
8. [Step 6: Verify Deployment](#step-6-verify-deployment)
9. [Troubleshooting](#troubleshooting)
10. [Monitoring & Maintenance](#monitoring--maintenance)

---

## Overview of Changes

### What Changed

The TRINETRA backend has been migrated from **MySQL** to **PostgreSQL** and configured for **Render cloud hosting**.

#### Configuration Updates:

**pom.xml**
- ✅ Removed: `mysql-connector-j` dependency
- ✅ Added: `org.postgresql:postgresql` dependency

**application.properties**
- ✅ Database URL: Changed to use `${DATABASE_URL}` environment variable
- ✅ Driver: `org.postgresql.Driver`
- ✅ Dialect: `org.hibernate.dialect.PostgreSQLDialect`
- ✅ Server Port: Uses `${PORT}` environment variable (default: 8080)
- ✅ JWT Secret: Uses `${JWT_SECRET}` environment variable
- ✅ Connection Pooling: Configured with HikariCP (5 max connections)
- ✅ Production Settings: `spring.jpa.open-in-view=false`, `spring.datasource.hikari.maximum-pool-size=5`

### Why These Changes?

| Change | Reason |
|--------|--------|
| PostgreSQL | Render provides managed PostgreSQL that's production-ready and scalable |
| Environment Variables | Render uses env vars for database credentials (DATABASE_URL) and port allocation |
| HikariCP Pool | Reduces connection overhead and improves performance on cloud |
| spring.jpa.open-in-view=false | Prevents lazy loading outside transaction scope (production best practice) |

---

## Prerequisites

### Local Machine
- ✅ Java 21 JDK installed
- ✅ Maven 3.8.1+ installed
- ✅ Git installed
- ✅ GitHub account with repo containing this code

### Render Account
- ✅ Render account created (https://render.com)
- ✅ Connected to GitHub repository
- ✅ Payment method added (for resource limit)

### Repository Requirements
- ✅ GitHub repository with the updated code
- ✅ `pom.xml` with PostgreSQL dependency
- ✅ `application.properties` with environment variable placeholders

---

## Step 1: Build the Application

### 1.1 Build JAR Locally

```bash
cd demo
mvn clean package -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXs
[INFO] Finished at: YYYY-MM-DDTHH:MM:SS
```

**Generated File:**
```
demo/target/demo.jar
```

### 1.2 Test JAR Locally (Optional)

```bash
# Set environment variables
$env:PORT=8081  # PowerShell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/trinetra"

# Run JAR
java -jar target/demo.jar
```

**Expected Console Output:**
```
Started Project1Application in X.XXX seconds
Server is running on port 8081
```

---

## Step 2: Prepare for Render

### 2.1 Ensure code is in GitHub

```bash
git add .
git commit -m "Configure for Render PostgreSQL deployment"
git push origin main
```

### 2.2 Create render.yaml (Optional but Recommended)

Create file: `demo/render.yaml`

```yaml
services:
  - type: web
    name: trinetra-backend
    env: java
    buildCommand: mvn clean package -DskipTests
    startCommand: java -jar target/demo.jar
    envVars:
      - key: DATABASE_URL
        fromDatabase:
          name: trinetra-db
          property: connectionString
      - key: JWT_SECRET
        sync: false
      - key: CORS_ALLOWED_ORIGINS
        value: https://your-frontend-domain.com,https://www.your-frontend-domain.com
      - key: PORT
        value: "8080"

databases:
  - name: trinetra-db
    databaseName: trinetra
    user: postgres
    plan: free
    region: us-east-1
```

### 2.3 Verify project structure

```
demo/
├── pom.xml (with PostgreSQL dependency)
├── src/
│   └── main/
│       ├── java/ (all controllers, services, entities)
│       └── resources/
│           ├── application.properties (with env vars)
│           └── data.sql (initial data)
├── target/
│   └── demo.jar (built package)
├── render.yaml (optional)
└── README.md
```

---

## Step 3: Create Render Resources

### 3.1 Create PostgreSQL Database

1. **Login to Render Dashboard**: https://dashboard.render.com

2. **Click "New +"** → Select "PostgreSQL"

3. **Configure Database:**
   - **Name**: `trinetra-db`
   - **Database**: `trinetra`
   - **User**: `postgres`
   - **Region**: Select closest to your users (e.g., `us-east-1`)
   - **Plan**: `Free` (for testing) or `Starter+` (for production)

4. **Click "Create Database"**

5. **Wait for Initialization** (2-5 minutes)

6. **Copy Connection String:**
   - Navigate to the database details page
   - Copy the **"External Database URL"**
   - Format: `postgresql://user:password@host:5432/database`

### 3.2 Create Web Service

1. **Click "New +"** → Select "Web Service"

2. **Connect Repository:**
   - Select GitHub account
   - Choose your repository
   - Click "Connect"

3. **Configure Web Service:**
   - **Name**: `trinetra-backend`
   - **Environment**: `Java`
   - **Region**: Same as database (e.g., `us-east-1`)
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/demo.jar`
   - **Plan**: `Free` or `Starter+`

4. **Do NOT Deploy Yet** - We need to add environment variables first!

5. **Click "Create Web Service"** (it will initially fail - that's OK)

---

## Step 4: Configure Environment Variables

### 4.1 Add Environment Variables in Render Dashboard

1. **Navigate to Web Service**: `trinetra-backend`

2. **Go to "Environment"** tab

3. **Add the following environment variables:**

| Key | Value | Notes |
|-----|-------|-------|
| `DATABASE_URL` | `postgresql://user:pass@host:port/db` | Copy from PostgreSQL database details |
| `JWT_SECRET` | `your-production-jwt-secret-key-min-32-chars` | Generate a strong secret |
| `PORT` | `8080` | Render will assign the actual port |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend-domain.com` | Update with your frontend domain |

### 4.2 Generate JWT Secret

Use a secure random string generator:

```bash
# PowerShell
-join ((1..64) | ForEach-Object {[char][int]((48..122) | Get-Random)})

# OR use OpenSSL (if installed)
openssl rand -base64 32
```

**Recommendation:** Use minimum 32 characters for production security.

### 4.3 Update Render Dashboard

1. **For each environment variable:**
   - Click "New Environment Variable"
   - Enter Key and Value
   - Click Add

2. **Your Render Environment Variables should look like:**
   ```
   DATABASE_URL: postgresql://postgres:xxxxx@dpg-xxx.xxxxx.render.com:5432/trinetra
   JWT_SECRET: your-very-secure-random-jwt-secret-key-min-32-chars
   PORT: 8080
   CORS_ALLOWED_ORIGINS: https://your-frontend-domain.com,https://www.your-frontend-domain.com
   ```

---

## Step 5: Deploy to Render

### 5.1 Trigger Manual Deployment

1. **Web Service Page**: `trinetra-backend`

2. **Click "Manual Deploy"** → **"Deploy latest commit"**

3. **Monitor Build Progress:**
   - Check "Logs" tab
   - Look for: `[INFO] BUILD SUCCESS`
   - Final line: `Started Project1Application in X.XXX seconds`

### 5.2 Watch for Deployment

Expected logs:

```
Building TRINETRA...
[INFO] Compiling...
[INFO] Building JAR...
[INFO] BUILD SUCCESS
Starting application...
Server starting...
Started Project1Application in 4.5 seconds
TRINETRA backend running on port 8080
```

### 5.3 Enable Auto-Deploy (Optional)

1. **Go to "Settings"** tab
2. **Enable "Auto-Deploy"** for automatic deployments on git push
3. **Save settings**

---

## Step 6: Verify Deployment

### 6.1 Check Service Status

1. **Render Dashboard** → Web Service
2. Verify status shows **"Live"** (green indicator)

### 6.2 Test Health Endpoint

```bash
# Get your Render URL from the web service page
# Format: https://trinetra-backend.onrender.com

curl https://trinetra-backend.onrender.com/api/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "service": "TRINETRA Backend",
  "database": "Connected",
  "timestamp": "2024-03-06T12:34:56"
}
```

### 6.3 Test Signup Endpoint

```bash
curl -X POST https://trinetra-backend.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "TestPassword123!"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGc...",
    "username": "test@example.com",
    "role": "USER"
  }
}
```

### 6.4 Test Report Submission

```bash
curl -X POST https://trinetra-backend.onrender.com/api/reports/submit \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Report",
    "category": "Harassment",
    "severity": "High",
    "description": "Testing anonymous report submission"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Report submitted successfully",
  "data": {
    "token": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

## Troubleshooting

### Issue 1: Build Fails with "PostgreSQL Driver Not Found"

**Solution:**
```bash
# Verify pom.xml has PostgreSQL dependency
mvn dependency:tree | grep postgresql

# If missing, check pom.xml has:
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

### Issue 2: "DATABASE_URL Not Found" Error

**Solution:**
1. Verify environment variable is set in Render Dashboard
2. Check exact spelling: `DATABASE_URL` (case-sensitive)
3. Ensure value starts with `postgresql://`
4. Redeploy after adding environment variable

### Issue 3: "Connection Refused" on Database

**Solution:**
```bash
# Test database URL format locally first
# DATABASE_URL should be: postgresql://user:password@host:port/database

# Verify from Render PostgreSQL details page
# Usually provided as: postgresql://postgres:xxxxx@dpg-xxx.xxxxx.render.com:5432/trinetra
```

### Issue 4: Slow Initial Startup

**Note:** First build/startup on Render takes longer (3-5 minutes). This is normal.

- Subsequent deployments are faster
- Keep an eye on build logs in Render Dashboard

### Issue 5: CORS Errors in Frontend

**Solution:**
1. Update `CORS_ALLOWED_ORIGINS` environment variable with your frontend domain
2. Do NOT include `/api` in the origin
3. Format: `https://yourdomain.com,https://www.yourdomain.com`
4. Redeploy after updating

### Issue 6: 500 Error from API Endpoints

**Solution:**
1. Check Render logs for the exact error
2. Most common: Missing environment variables
3. Verify `JWT_SECRET` is set and is at least 32 characters
4. Restart service after adding env vars

---

## Monitoring & Maintenance

### View Logs

1. **Render Dashboard** → Web Service
2. **Click "Logs"** tab
3. See real-time application logs

### Monitor Database

1. **Render Dashboard** → PostgreSQL Database
2. **Monitor Usage:**
   - Connections
   - Storage
   - CPU usage

### Update Database

To connect to PostgreSQL directly (for data inspection):

```bash
# Get connection string from Render PostgreSQL details
# Format: postgresql://user:password@host:port/database

psql postgresql://user:password@host:port/database

# Common queries
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM reports;
SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 10;
```

### Backup Data

Render provides automatic daily backups for PostgreSQL. To restore:

1. **Render Dashboard** → PostgreSQL Database
2. **Go to "Backups"** tab
3. **Click "Restore"** on desired backup

### Update Application

**Deployment Methods:**

**Option 1: Auto-Deploy (Recommended)**
```bash
# Make changes locally
git add .
git commit -m "Update backend feature"
git push origin main

# Render automatically deploys changes
```

**Option 2: Manual Deploy**
1. Render Dashboard → Web Service
2. Click "Manual Deploy" → "Deploy latest commit"

### Scaling

**For Production Traffic:**

1. Upgrade Web Service Plan:
   - Render Dashboard → Settings
   - Choose higher plan (e.g., "Starter+")

2. Upgrade Database Plan:
   - Render Dashboard → PostgreSQL Database
   - Choose higher plan

---

## Environment Variables Reference

### Required Variables

```bash
DATABASE_URL=postgresql://user:password@host:port/database
JWT_SECRET=your-production-jwt-secret-min-32-chars
```

### Optional Variables (with defaults)

```bash
PORT=8080  # Render sets this automatically
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

### Development vs Production

| Variable | Development | Production |
|----------|-------------|-----------|
| `PORT` | 8081 | 8080 (Render managed) |
| `DATABASE_URL` | localhost:5432 | Render PostgreSQL external URL |
| `JWT_SECRET` | `trinetra-secret-key` | Strong random 32+ char string |
| `CORS_ALLOWED_ORIGINS` | localhost:3000,localhost:4200 | your-frontend-domain.com |

---

## Security Checklist

✅ **Before Production Deployment:**

- [ ] JWT_SECRET is 32+ characters
- [ ] DATABASE_URL uses secure connection (postgresql://)
- [ ] No hardcoded credentials in code
- [ ] CORS_ALLOWED_ORIGINS only includes your domains
- [ ] Database backups are enabled
- [ ] Environment variables are NOT publicly visible
- [ ] HTTPS is enabled (Render default)
- [ ] Spring Security JWT filter is active
- [ ] Admin routes require authentication

✅ **Post-Deployment Verification:**

- [ ] Health endpoint returns database status "Connected"
- [ ] Auth endpoints work (signup/login)
- [ ] Token generation works
- [ ] Report submission works
- [ ] Admin endpoints are protected
- [ ] Audit logs are being recorded

---

## Performance Tips

1. **Database Connection Pool:**
   - Maximum pool size: 5 (production)
   - Monitor connection usage in Render Dashboard

2. **Logging:**
   - Set `logging.level.root=INFO` for production
   - Reduce debug logging to improve performance

3. **Caching:**
   - Consider adding Redis for token caching (future enhancement)

4. **Database Indexes:**
   - Ensure proper indexes on frequently queried columns
   - Example: `CREATE INDEX idx_token ON reports(token);`

---

## Additional Resources

- **Render Documentation**: https://render.com/docs
- **PostgreSQL on Render**: https://render.com/docs/databases
- **Spring Boot & Render**: https://render.com/docs/deploy-springboot
- **Environment Variables**: https://render.com/docs/environment-variables

---

## Support & Troubleshooting

### Where to Get Help

1. **Render Support**: https://render.com/support
2. **Spring Boot Documentation**: https://spring.io/projects/spring-boot
3. **PostgreSQL Documentation**: https://www.postgresql.org/docs/
4. **Check Render Logs**: Dashboard → Logs tab

### Common Commands

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Build locally
mvn clean package -DskipTests

# Run locally with env vars
$env:DATABASE_URL="postgresql://..."
$env:JWT_SECRET="your-secret-key"
java -jar target/demo.jar
```

---

## Deployment Success Checklist

✅ **Completed Steps:**

- [ ] PostgreSQL dependency added to pom.xml
- [ ] application.properties updated with env variables
- [ ] Render PostgreSQL database created
- [ ] Render Web Service created
- [ ] Environment variables configured (DATABASE_URL, JWT_SECRET)
- [ ] Application deployed successfully
- [ ] Health endpoint returns "Connected"
- [ ] Auth endpoints functional
- [ ] Report submission working
- [ ] CORS configured for frontend domain
- [ ] Monitoring logs reviewed

---

**Version**: 1.0.0  
**Last Updated**: March 6, 2024  
**Status**: Production Ready for Render PostgreSQL Deployment
