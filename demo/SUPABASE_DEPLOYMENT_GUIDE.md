# Supabase PostgreSQL Migration & Deployment Guide

## Overview

This guide walks you through migrating the TRINETRA backend from Render PostgreSQL to Supabase PostgreSQL. The migration involves:

1. Creating a Supabase project and PostgreSQL database
2. Initializing the database schema
3. Configuring environment variables for local development and Render deployment
4. Testing database connectivity
5. Deploying to Render with Supabase backend

---

## Step 1: Create Supabase Project

### 1.1 Sign Up / Log In to Supabase
- Go to [supabase.com](https://supabase.com)
- Click "Start your project" or sign in with GitHub
- Create a new account if needed

### 1.2 Create New Project
- Click "New Project" in your dashboard
- Select your organization
- Enter project name: `trinetra-prod` (or `trinetra-dev`)
- Set database password (save this securely!)
- Select region closest to your users (e.g., US East for most users)
- Click "Create new project"

### 1.3 Wait for Database Initialization
- Supabase will create your PostgreSQL instance (takes ~2 minutes)
- You'll see the project dashboard once ready

---

## Step 2: Get Database Connection Details

### 2.1 Access Connection Info
1. In Supabase dashboard, go to **Settings > Database**
2. Under "Connection string" you'll see the PostgreSQL connection details:
   - **Host**: `db.XXXXXXXXXXXXX.supabase.co` (keep this!)
   - **Port**: `5432`
   - **Database**: `postgres`
   - **User**: `postgres`
   - **Password**: (the one you set during project creation)

### 2.2 Connection String Format
Supabase provides two formats:

**PostgreSQL Format** (for DATABASE_URL env var):
```
postgresql://postgres:PASSWORD@db.XXXXXXXXXXXXX.supabase.co:5432/postgres
```

**JDBC Format** (for Spring Boot):
```
jdbc:postgresql://db.XXXXXXXXXXXXX.supabase.co:5432/postgres?sslmode=require
```

**Important**: Supabase **requires SSL** connections. The `sslmode=require` parameter is mandatory.

### 2.3 Example Values
For this project:
- **Host**: `db.hbnjxekpkomrezeiogxm.supabase.co`
- **PostgreSQL URL**: `postgresql://postgres:YOUR_PASSWORD@db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres`
- **JDBC URL**: `jdbc:postgresql://db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres?sslmode=require`

---

## Step 3: Initialize Database Schema

### 3.1 Run SQL Script in Supabase
1. In Supabase dashboard, go to **SQL Editor**
2. Click "New Query"
3. Open file: `SUPABASE_SCHEMA.sql` (in your project root)
4. Copy the entire contents
5. Paste into Supabase SQL Editor
6. Click "Run" (play button, or Ctrl+Enter)
7. Verify all tables created successfully

### 3.2 Verify Schema
After running the script, verify in Supabase:
1. Go to **Database > Tables**
2. You should see:
   - users
   - reports
   - evidence
   - audit_logs
   - notifications
   - ai_investigation_log

All tables should have data populated (admin user created automatically).

### 3.3 Check Default Admin User
1. Go to **SQL Editor** > New Query
2. Run:
   ```sql
   SELECT id, username, email, role, status FROM users;
   ```
3. You should see the admin user created by the schema script

---

## Step 4: Configure Local Development

### 4.1 Create `.env` File (Local Development)
In your project root (`demo/` folder), create a new file called `.env`:

```properties
# Supabase Database Connection (PostgreSQL format)
DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres

# Alternative: Use component-based variables
DB_HOST=db.hbnjxekpkomrezeiogxm.supabase.co
DB_PORT=5432
DB_NAME=postgres
DB_USERNAME=postgres
DB_PASSWORD=YOUR_PASSWORD

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-in-production
JWT_EXPIRATION=86400000

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# Server Configuration
PORT=8080
```

**⚠️ Important**: 
- Replace `YOUR_PASSWORD` with your actual Supabase password
- Never commit `.env` to Git (add to `.gitignore`)
- Use different passwords for dev/prod

### 4.2 Update `.gitignore`
Ensure your `.gitignore` includes:
```
.env
.env.local
.env.*.local
```

### 4.3 Load Environment Variables in IDE
For VS Code or IntelliJ, you can set environment variables:

**VS Code** - Create `.vscode/launch.json`:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "name": "Spring Boot App",
            "type": "java",
            "name": "Spring Boot App",
            "request": "launch",
            "cwd": "${workspaceFolder}",
            "mainClass": "com.example.demo.Project1Application",
            "projectName": "demo",
            "args": "",
            "env": {
                "DATABASE_URL": "postgresql://postgres:YOUR_PASSWORD@db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres",
                "JWT_SECRET": "dev-secret-key-change-in-production"
            }
        }
    ]
}
```

---

## Step 5: Test Local Database Connection

### 5.1 Build the Project
```bash
cd demo
mvn clean compile
```

### 5.2 Run Spring Boot Application Locally
```bash
mvn spring-boot:run
```

### 5.3 Monitor Logs
Watch for:
- ✅ `Created connection pool with ...`
- ✅ `HibernateJpaConfiguration : Initializing JPA EntityManagerFactory`
- ✅ `Tomcat started on port 8080`
- ❌ `Database connection failed` → Check password, network
- ❌ `SSL error` → Ensure `sslmode=require` in JDBC URL

### 5.4 Test API Endpoint
```bash
curl http://localhost:8080/actuator/health
```

Should return:
```json
{
  "status": "UP"
}
```

---

## Step 6: Configure Render Environment Variables

### 6.1 Go to Render Dashboard
1. Navigate to https://dashboard.render.com
2. Select your TRINETRA service
3. Go to **Environment**

### 6.2 Set Required Variables
Add these environment variables in Render:

| Variable | Value | Example |
|----------|-------|---------|
| `DATABASE_URL` | PostgreSQL connection string | `postgresql://postgres:password@db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres` |
| `DB_USERNAME` | postgres | `postgres` |
| `DB_PASSWORD` | Your Supabase password | `xxxxxxxx` |
| `JWT_SECRET` | Long random string for JWT | `your-production-jwt-secret-min-32-chars` |
| `JWT_EXPIRATION` | Token expiry in milliseconds | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Frontend URLs | `https://yourdomain.com,https://app.yourdomain.com` |

### 6.3 Important Notes
- Use **strong, unique passwords** for production
- Don't reuse your local development passwords
- Consider rotating passwords every 90 days
- Use Render's built-in secrets manager if available

### 6.4 Save Configuration
Click "Save" or "Deploy" to apply environment variables.

---

## Step 7: Deploy to Render

### 7.1 Commit Your Changes
```bash
git add pom.xml application.properties src/main/java/com/safevoice/config/DataSourceConfiguration.java
git commit -m "Configure Supabase PostgreSQL and fix DataSource bean initialization"
git push origin main
```

### 7.2 Trigger Deployment
1. In Render dashboard, your service should auto-detect the push
2. Deployment will start automatically
3. Watch the deployment logs in real-time

### 7.3 Monitor Deployment
Key logs to look for:

✅ **Success indicators**:
```
Created connection pool with HikariPool-1 (huh...)
HibernateJpaConfiguration : Initializing JPA EntityManagerFactory
Hibernate: Sequence initialized with value: 1
Tomcat started on port 8080 with context path ''
```

❌ **Failure indicators**:
```
Could not get a resource from the pool
FATAL: remaining connection slots are reserved for non-replication superuser connections
User-defined bean method 'dataSource' in 'DataSourceConfiguration' ignored as the bean value is null
```

### 7.4 Troubleshooting Common Issues

**Issue**: "Connection refused" or "connect timed out"
- Check if DATABASE_URL environment variable is set in Render
- Verify Supabase IP whitelist includes Render's IP ranges
- Check Supabase firewall rules

**Issue**: "Invalid parameters: Database connection string or host is invalid"
- Ensure DATABASE_URL format is correct: `postgresql://user:pass@host:port/db`
- Don't use JDBC format in DATABASE_URL variable (DataSourceConfiguration handles conversion)
- Check password doesn't contain special characters that need URL encoding

**Issue**: "SSL/TLS error"
- Ensure `sslmode=require` is in the JDBC URL (DataSourceConfiguration adds this)
- Check Supabase hasn't changed SSL certificate

**Issue**: "User 'postgres' cannot be used"
- Supabase user is always 'postgres' (not your email)
- Check DB_USERNAME is set to 'postgres'

---

## Step 8: Verify Deployment

### 8.1 Check Application Health
```bash
curl https://your-render-service.onrender.com/actuator/health
```

Should return:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL"
      }
    }
  }
}
```

### 8.2 Test Authentication Endpoints
```bash
# Create a new user
curl -X POST https://your-render-service.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123!",
    "fullName": "Test User"
  }'

# Login
curl -X POST https://your-render-service.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!"
  }'
```

### 8.3 Check Database in Supabase
1. Go to Supabase dashboard
2. Open **SQL Editor**
3. Run:
   ```sql
   SELECT COUNT(*) as user_count FROM users;
   SELECT COUNT(*) as report_count FROM reports;
   ```
4. Verify data is being created by your application

---

## Step 9: Security Best Practices

### 9.1 Change Default Admin Password
The schema creates a default admin user with a hardcoded password hash. Change it:

**For production**, update this in Supabase SQL Editor:
```sql
-- Update admin password (use a real BCrypt hash!)
UPDATE users 
SET password_hash = '$2a$10$YOUR_BCRYPT_HASH_HERE'
WHERE username = 'admin';
```

To generate a BCrypt hash:
- Use online tool: https://bcrypt-generator.com
- Or use Java: `new BCryptPasswordEncoder().encode("YourPassword")`

### 9.2 Supabase Security Settings
1. Go to **Settings > Database**
2. Review "Connection pooler" settings
3. Enable "SSL enforcement" if available
4. Review access logs periodically

### 9.3 Rotate Secrets Regularly
- Change JWT_SECRET every 6 months
- Change database password every 90 days
- Monitor audit_logs table for suspicious activity

### 9.4 Network Security
- Limit Supabase access to specific IP ranges
- Use VPN for secure local database access
- Never expose DATABASE_URL in client-side code

---

## Configuration Reference

### Environment Variables Summary

```bash
# Database Configuration
DATABASE_URL=postgresql://postgres:PASSWORD@db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=PASSWORD
DB_HOST=db.hbnjxekpkomrezeiogxm.supabase.co
DB_PORT=5432
DB_NAME=postgres
DB_POOL_SIZE=5

# JWT Configuration
JWT_SECRET=min-32-characters-for-production
JWT_EXPIRATION=86400000

# Server Configuration
PORT=8080
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
```

### Spring Boot Properties File Defaults
See `application.properties` for:
- HikariCP pool settings
- Hibernate DDL strategy (update)
- JPA logging levels
- CORS configuration

---

## Rollback Plan

If you need to revert to Render PostgreSQL:

1. **Backup Supabase data** (export SQL from Supabase)
2. **Revert code changes**:
   ```bash
   git revert <commit-hash>
   git push origin main
   ```
3. **Reconfigure Render**:
   - Set DATABASE_URL to old Render Postgres connection string
   - Render will auto-redeploy

---

## Support & Troubleshooting

### Documentation Links
- Supabase: https://supabase.com/docs
- PostgreSQL: https://www.postgresql.org/docs/
- Spring Boot Database: https://spring.io/guides/gs/accessing-data-jpa/
- HikariCP: https://github.com/brettwooldridge/HikariCP/wiki

### Check Logs
**Supabase Logs**:
- Dashboard > Logs > Postgres Logs

**Render Logs**:
- Service Dashboard > Logs

**Local Logs**:
```bash
mvn spring-boot:run | grep -i "database\\|connection\\|error"
```

---

## Checklist - Pre-Deployment

- [ ] Supabase project created and database initialized
- [ ] SUPABASE_SCHEMA.sql executed successfully
- [ ] Local `.env` file created with test credentials
- [ ] Application runs locally connected to Supabase
- [ ] All tables visible in Supabase dashboard
- [ ] Environment variables set in Render dashboard
- [ ] Application compiles: `mvn clean compile`
- [ ] Code committed and pushed to GitHub
- [ ] Render deployment completed successfully
- [ ] Production health endpoint responds
- [ ] Test user can be created and authenticated
- [ ] Database queries return expected data

---

## Next Steps

1. **Configure Frontend**: Update API endpoints to point to your Render service
2. **Set Up HTTPS**: Enable custom domain with SSL on Render
3. **Configure Email**: Set up notification emails via SendGrid or similar
4. **Monitor Performance**: Set up alerts for database performance
5. **Backup Strategy**: Configure automated Supabase backups

---

**Last Updated**: 2024  
**Schema Version**: 1.0  
**Database**: Supabase PostgreSQL  
**Deployment Platform**: Render  

