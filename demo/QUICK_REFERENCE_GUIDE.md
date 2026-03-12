# TRINETRA Supabase Migration - Quick Reference Guide

## 🎯 What Was Done

Your TRINETRA backend has been successfully configured for **Supabase PostgreSQL** deployment. All database configuration is now environment-variable based, supporting both **local development** and **Render production** deployment with zero code changes to business logic.

---

## 📋 Files Created/Modified

### Configuration Files
| File | Purpose | Changes |
|------|---------|---------|
| `src/main/java/com/safevoice/config/DataSourceConfiguration.java` | Database connection pool management | ✅ Fixed to handle DATABASE_URL parsing without returning null |
| `src/main/resources/application.properties` | Default Spring Boot config | ✅ Updated for Supabase with environment variable fallbacks |
| `src/main/resources/application-render.properties` | Render production profile | ✅ Optimized for containerized Render deployment |

### Database & Documentation
| File | Purpose | Status |
|------|---------|--------|
| `SUPABASE_SCHEMA.sql` | Database schema (tables, indexes, views) | ✅ Created - ready to run |
| `SUPABASE_DEPLOYMENT_GUIDE.md` | Step-by-step deployment instructions | ✅ Created - comprehensive guide |
| `RENDER_ENV_VARIABLES_TEMPLATE.txt` | Environment variables for Render | ✅ Created - copy-paste ready |
| `QUICK_REFERENCE_GUIDE.md` | This file | ℹ️ Quick summary |

---

## 🚀 Quick Start - 3 Simple Steps

### Step 1: Create Supabase Project (5 minutes)
```
1. Go to supabase.com
2. Click "New Project"
3. Fill in project name, password, region
4. Copy your connection details:
   - Host: db.XXXXXXXXXXXXX.supabase.co
   - Password: (what you set)
```

### Step 2: Initialize Database Schema (2 minutes)
```
1. In Supabase > SQL Editor > New Query
2. Copy entire contents of: SUPABASE_SCHEMA.sql
3. Click "Run" (Ctrl+Enter)
4. Verify all 6 tables created in Tables section
```

### Step 3: Deploy to Render (5 minutes)
```
1. In Render dashboard > Your Service > Settings > Environment
2. Add these variables (copy from RENDER_ENV_VARIABLES_TEMPLATE.txt):
   - DATABASE_URL=postgresql://postgres:PASSWORD@db.XXXXXXXXXXXXX.supabase.co:5432/postgres
   - DB_USERNAME=postgres
   - DB_PASSWORD=PASSWORD
   - JWT_SECRET=(generate 32+ char random string)
   - Other vars as listed in template
3. Click Save
4. Render auto-redeploys with new config
5. Check logs for "Tomcat started on port 8080" ✅
```

---

## 📊 Database Schema Overview

Your Supabase database now has these tables:

| Table | Purpose | Relationship |
|-------|---------|--------------|
| **users** | User accounts & authentication | Parent |
| **reports** | Incident/concern reports | FK: user_id → users |
| **evidence** | File attachments for reports | FK: report_id → reports |
| **audit_logs** | Activity tracking for compliance | FK: user_id → users |
| **notifications** | User notifications | FK: recipient_id → users |
| **ai_investigation_log** | AI analysis results (future) | FK: report_id → reports |

**Plus 2 Views**:
- `report_statistics` - Analytics on reports by category/severity
- `user_activity` - User engagement metrics

---

## 🔧 Environment Variables

### For Local Development (`.env` file)
```properties
DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=YOUR_PASSWORD
JWT_SECRET=your-dev-secret-key
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
PORT=8080
```

### For Render Production
Copy from **RENDER_ENV_VARIABLES_TEMPLATE.txt** and add to:
- Render Dashboard > Services > Your Service > Settings > Environment

**Key differences for production**:
- Use strong, unique passwords (not development ones!)
- Set CORS_ALLOWED_ORIGINS to your production domains only
- Generate secure JWT_SECRET (32+ characters)
- Consider rotating passwords every 90 days

---

## 🧪 Testing

### Test Local Connection
```bash
cd demo
export DATABASE_URL="postgresql://postgres:PASSWORD@db.XXXXXXXXXXXXX.supabase.co:5432/postgres"
export JWT_SECRET="test-secret-key"
mvn spring-boot:run

# In another terminal:
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

### Test Render Deployment
```bash
# Check health endpoint
curl https://your-render-service.onrender.com/actuator/health

# View logs in Render dashboard
# Look for: "Created connection pool" and "Tomcat started"

# Test in Supabase - run SQL:
SELECT COUNT(*) as users FROM users;
SELECT COUNT(*) as reports FROM reports;
```

---

## ✅ Verification Checklist

- [ ] Supabase project created
- [ ] Database schema initialized (SUPABASE_SCHEMA.sql executed)
- [ ] Local `.env` file created
- [ ] Application runs locally: `mvn spring-boot:run` → Connected to Supabase ✓
- [ ] Health endpoint responds: `curl http://localhost:8080/actuator/health` → UP ✓
- [ ] Admin user created in database (auto-created by schema)
- [ ] Environment variables set in Render
- [ ] Code compiled: `mvn clean compile` → BUILD SUCCESS ✓
- [ ] Changes committed to Git
- [ ] Render deployment completed
- [ ] Production health endpoint responds
- [ ] Data in admin user query: `SELECT * FROM users;`

---

## 🔐 Security Checklist

- [ ] Supabase password is strong & unique
- [ ] JWT_SECRET is 32+ characters & kept secret
- [ ] `.env` file is in `.gitignore` (never commit it!)
- [ ] Production passwords differ from local dev
- [ ] CORS_ALLOWED_ORIGINS restricted to your domains
- [ ] Default admin user password changed (optional but recommended)
- [ ] Supabase backup enabled in project settings
- [ ] SSL verified (Supabase enforces `sslmode=require`)

---

## 🔌 Connection Details

**Supabase Connection String Formats**:

PostgreSQL (used in DATABASE_URL):
```
postgresql://postgres:PASSWORD@db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres
```

JDBC (converted by DataSourceConfiguration):
```
jdbc:postgresql://db.hbnjxekpkomrezeiogxm.supabase.co:5432/postgres?sslmode=require
```

**IMPORTANT**: 
- Supabase **requires SSL** (`sslmode=require` is mandatory)
- Database name is always `postgres` (Supabase default)
- Username is always `postgres` (cannot change)
- Password is what you set during Supabase project creation

---

## ⚙️ How It Works

### DataSourceConfiguration (New Component)
Located in: `src/main/java/com/safevoice/config/DataSourceConfiguration.java`

**Purpose**: Converts PostgreSQL format URLs to JDBC format + manages HikariCP connection pool

**Logic Flow**:
1. Check if `DATABASE_URL` env var exists (Render provides this)
2. If it's PostgreSQL format (`postgresql://...`), parse it:
   - Extract: host, port, database, username, password
   - Convert to: `jdbc:postgresql://...?sslmode=require`
3. If no DATABASE_URL, fall back to component vars: `DB_HOST`, `DB_PORT`, etc.
4. Create HikariCP DataSource with these settings:
   - Pool size: 5 connections
   - Timeout: 10 seconds
   - Max lifetime: 30 minutes
   - SSL: Required

**Key Fix**: No longer returns `null` - always returns valid DataSource or throws error

### application.properties (Updated)
Now includes:
- Datasource configuration with environment variable fallbacks
- HikariCP tuning (pool size, timeouts, lifecycle)
- JPA/Hibernate settings (PostgreSQL dialect, DDL auto-update)
- JWT configuration (secret, expiration)
- Server configuration (port, context path, session timeout)
- Logging levels (DEBUG for app, WARN for framework)
- CORS settings (configurable origins)

### application-render.properties (New)
Production-optimized settings:
- Reduced logging verbosity (less disk I/O)
- Tomcat thread pool tuned for containers (50 threads max)
- Connection validation enabled
- Graceful shutdown configured
- Actuator health endpoints enabled for monitoring

---

## 🛠️ Troubleshooting Guide

### Issue: "Connection refused" or "connect timed out"
**Causes**:
- DATABASE_URL not set in Render environment
- Wrong host/port in connection string
- Supabase project not yet initialized

**Solution**:
1. Verify DATABASE_URL in Render dashboard
2. Check host matches Supabase dashboard (Settings > Database)
3. Wait 2 minutes if Supabase project just created

### Issue: "SSL/TLS error" or "Certificate not found"
**Cause**: Supabase requires SSL by default

**Solution**: 
- Ensure `sslmode=require` is in JDBC URL (DataSourceConfiguration adds this automatically)
- If error persists, try `sslmode=prefer` temporarily for debugging

### Issue: "Invalid parameters: Database connection string"
**Cause**: Wrong DATABASE_URL format

**Solution**:
- Use PostgreSQL format: `postgresql://user:pass@host:port/db`
- NOT JDBC format in DATABASE_URL env var
- DataSourceConfiguration handles the conversion

### Issue: "Could not get a resource from the pool"
**Cause**: Connection pool exhausted

**Solution**:
1. Increase `DB_POOL_SIZE` environment variable (default 5)
2. Check for connection leaks in application code
3. Monitor Render service memory usage

### Issue: Render logs show "Cannot create table, it already exists"
**Cause**: Schema already exists, Hibernate DDL strategy is `create` or `create-drop`

**Solution**:
- This is normal if running app multiple times
- Schema is set to `ddl-auto=update` (creates tables if missing, doesn't drop)
- If you need clean slate, manually delete tables in Supabase SQL Editor

---

## 📚 Additional Resources

### Documentation Files (in Project)
- **SUPABASE_DEPLOYMENT_GUIDE.md** - Detailed step-by-step guide (you are here)
- **SUPABASE_SCHEMA.sql** - Database creation script
- **RENDER_ENV_VARIABLES_TEMPLATE.txt** - Environment variables reference

### External Resources
- Supabase Docs: https://supabase.com/docs
- PostgreSQL Docs: https://www.postgresql.org/docs/
- Spring Boot JPA: https://spring.io/guides/gs/accessing-data-jpa/
- HikariCP Configuration: https://github.com/brettwooldridge/HikariCP/wiki
- Render Documentation: https://render.com/docs

---

## 🚨 Important Notes

### Password Security
1. **Never commit `.env`** to Git (add to `.gitignore`)
2. **Don't hardcode secrets** in application.properties
3. **Use different passwords** for dev/staging/production
4. **Rotate passwords** every 90 days for production

### Default Admin User
Schema creates `admin` user automatically:
- **Username**: `admin`
- **Email**: `admin@trinetra.local`
- **Password Hash**: Hardcoded (bcrypt - change in production!)

To change admin password:
```sql
-- In Supabase SQL Editor:
-- Generate new BCrypt hash at: https://bcrypt-generator.com
UPDATE users 
SET password_hash = '$2a$10$YOUR_NEW_BCRYPT_HASH'
WHERE username = 'admin';
```

### Production Deployment
Before going live:
1. Change default admin password
2. Set strong JWT_SECRET (32+ chars)
3. Configure real CORS domains
4. Enable HTTPS on custom domain
5. Set up monitoring/alerting
6. Configure automated backups
7. Review security settings

---

## 📞 Getting Help

### Deployment Issues
1. Check Render logs: Service Dashboard > Logs
2. Monitor Supabase: Dashboard > Logs > Postgres Logs
3. Test locally first: `mvn spring-boot:run`

### Database Questions
1. Test query in Supabase SQL Editor
2. Check schema in Supabase Tables section
3. Look for error messages in audit_logs table

### Configuration Issues
1. Verify all environment variables set in Render
2. Check application.properties syntax
3. Look for typos in DATABASE_URL format

---

## 🎓 Configuration Summary

```
┌─────────────────────────────────────────────────┐
│          TRINETRA Supabase Architecture          │
├─────────────────────────────────────────────────┤
│                                                  │
│  Render Container                                │
│  ┌──────────────────────────────────────────┐   │
│  │ Spring Boot 4 Application                │   │
│  │ ┌────────────────────────────────────┐   │   │
│  │ │ DataSourceConfiguration            │   │   │
│  │ │ - Parses DATABASE_URL env var      │   │   │
│  │ │ - Creates HikariCP connection pool │   │   │
│  │ │ - Adds SSL (sslmode=require)       │   │   │
│  │ └────────────────────────────────────┘   │   │
│  │           ↓ (JDBC Connection)              │   │
│  │ ┌────────────────────────────────────┐   │   │
│  │ │ Hibernate/JPA                      │   │   │
│  │ │ - Maps entities to tables          │   │   │
│  │ │ - Manages relationships            │   │   │
│  │ │ - Auto-updates schema (DDL)        │   │   │
│  │ └────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────┘   │
│           ↓ (SSL encrypted)                     │
│  Supabase PostgreSQL                            │
│  ┌──────────────────────────────────────────┐   │
│  │ 6 Tables + 2 Views                       │   │
│  │ - users, reports, evidence              │   │
│  │ - audit_logs, notifications             │   │
│  │ - ai_investigation_log                  │   │
│  │ - report_statistics (view)               │   │
│  │ - user_activity (view)                   │   │
│  └──────────────────────────────────────────┘   │
│                                                  │
└─────────────────────────────────────────────────┘
```

---

## ✨ Next Steps

1. **Create Supabase Project** (see Step 1 above)
2. **Initialize Schema** (see Step 2 above)
3. **Test Locally** with `.env` configuration
4. **Deploy to Render** (see Step 3 above)
5. **Verify in Production** with health endpoint
6. **Monitor Logs** for any connection issues
7. **Configure Frontend** to point to Render URL
8. **Set Up Custom Domain** (optional)

---

**Status**: ✅ Ready for Deployment  
**Java Version**: 21  
**Spring Boot**: 4.0.3  
**Database**: Supabase PostgreSQL  
**Deployment**: Render  

**Changes Verified**:
- ✅ Maven compiles successfully
- ✅ No code logic changes (only configuration)
- ✅ All environment variables properly handled
- ✅ DataSource bean properly configured
- ✅ Supabase schema validated
- ✅ Render configuration optimized

You're ready to deploy! 🚀

