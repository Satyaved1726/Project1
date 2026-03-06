# TRINETRA - Render Environment Variables Configuration

## Quick Setup Reference

This file contains all environment variables needed for deploying TRINETRA Backend on Render with PostgreSQL.

---

## Required Environment Variables

### 1. DATABASE_URL (REQUIRED)

**Source**: Render PostgreSQL Database Details Page

**Format**: 
```
postgresql://username:password@hostname:port/database
```

**Example**:
```
postgresql://postgres:abc123xyz@dpg-abc123.xxxx.render.com:5432/trinetra
```

**How to Find**:
1. Go to Render Dashboard
2. Click on PostgreSQL database (e.g., `trinetra-db`)
3. Copy "External Database URL" from details section
4. This is your `DATABASE_URL`

**Note**: 
- Starts with `postgresql://` (not `mysql://`)
- Includes credentials and port
- Application will parse this automatically

---

### 2. JWT_SECRET (REQUIRED)

**Purpose**: Sign and verify JWT authentication tokens

**Requirements**:
- Minimum 32 characters
- Alphanumeric + special characters
- Unique for your deployment
- Keep it secret!

**Generate Secure Secret**:

**Option A - PowerShell**:
```powershell
-join ((1..64) | ForEach-Object {[char][int]((48..122) | Get-Random)})
```

**Option B - OpenSSL**:
```bash
openssl rand -base64 32
```

**Option C - Online Generator**:
Visit: https://www.random.org/strings/

**Example** (Never use this - generate your own):
```
a7Kp3mQxL9wRtY2sZ4bCnH6vF1jG8dE5oPuIsAq0
```

**How to Add**:
1. Render Dashboard → Web Service Settings
2. Environment Variables tab
3. Click "Add Environment Variable"
4. Key: `JWT_SECRET`
5. Value: Your generated secret
6. Save and redeploy

---

## Optional Environment Variables

### 3. PORT (Auto-set by Render)

**Purpose**: Configure application listening port

**Default**: `8080`

**Format**: Numeric (e.g., `8080`)

**Override in Render**:
```
PORT=8080
```

**Note**: 
- Render automatically assigns a port
- This defaults to 8080 if not set
- Usually no need to override

---

### 4. CORS_ALLOWED_ORIGINS (Optional)

**Purpose**: Allow your frontend domain to make requests

**Default**: `http://localhost:3000,http://localhost:4200`

**Format**: Comma-separated URLs (NO trailing slashes)
```
https://yourdomain.com,https://www.yourdomain.com
```

**Examples**:

```
# Single domain
https://myapp.com

# Multiple domains
https://myapp.com,https://www.myapp.com,https://staging.myapp.com

# Multiple subdomains
https://app.example.com,https://api.example.com

# Mix of domains
https://example.com,https://www.example.com,https://mobile.example.com
```

**How to Add**:
1. Render Dashboard → Web Service Settings
2. Environment Variables tab
3. Key: `CORS_ALLOWED_ORIGINS`
4. Value: Your frontend domains
5. Save and redeploy

**Important**:
- ✅ DO include: `https://yourdomain.com`
- ✅ DO include: `https://www.yourdomain.com` (if used)
- ❌ DO NOT include: `/api` in the origin
- ❌ DO NOT include: Protocol path like `/api/v1`
- ❌ DO NOT use: `http://` in production (must be `https://`)

---

## Step-by-Step Setup on Render

### Step 1: Create PostgreSQL Database

1. Render Dashboard → "New +" → "PostgreSQL"
2. Enter:
   - **Name**: `trinetra-db`
   - **Database**: `trinetra`
   - **User**: `postgres`
   - **Region**: Your preferred region
3. Click "Create Database"
4. Wait for initialization (2-5 minutes)
5. **Copy the External Database URL** → This is your `DATABASE_URL`

### Step 2: Create Web Service

1. Render Dashboard → "New +" → "Web Service"
2. Connect your GitHub repository
3. Enter:
   - **Name**: `trinetra-backend`
   - **Environment**: `Java`
   - **Region**: Same as database
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/demo.jar`
4. **DO NOT deploy yet**

### Step 3: Add Environment Variables

1. Web Service page → "Environment" tab
2. Click "Add Environment Variable" for each:

```
Key: DATABASE_URL
Value: postgresql://postgres:xxxxx@dpg-xxx.xxxxx.render.com:5432/trinetra

Key: JWT_SECRET
Value: your-generated-32-character-secret-key-here

Key: CORS_ALLOWED_ORIGINS
Value: https://your-frontend-domain.com,https://www.your-frontend-domain.com

Key: PORT
Value: 8080
```

3. Click "Save" after adding all variables

### Step 4: Deploy

1. Web Service page → "Deploy" → "Manual Deploy"
2. Watch logs for success:
   - Should see: `[INFO] BUILD SUCCESS`
   - Should see: `Started Project1Application`
3. When status becomes "Live" (green) → Deployment successful

---

## Verification After Setup

### Test Health Endpoint

```bash
curl https://your-render-url.onrender.com/api/health
```

Should return:
```json
{
  "status": "UP",
  "service": "TRINETRA Backend",
  "database": "Connected",
  "timestamp": "2024-03-06T12:34:56"
}
```

### Test Authentication

```bash
curl -X POST https://your-render-url.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Test@1234"
  }'
```

---

## Environment Variables Checklist

### Before First Deployment

- [ ] `DATABASE_URL` - Copied from Render PostgreSQL database
- [ ] `JWT_SECRET` - Generated (32+ characters)
- [ ] `CORS_ALLOWED_ORIGINS` - Set to your frontend domain(s)
- [ ] `PORT` - Optional (defaults to 8080)

### Verify in Render Dashboard

```
Environment tab should show:
✓ DATABASE_URL: postgresql://...
✓ JWT_SECRET: (hidden/obfuscated)
✓ CORS_ALLOWED_ORIGINS: https://yourdomain.com
✓ PORT: 8080
```

### After Deployment

- [ ] Health endpoint returns "Connected"
- [ ] Auth endpoints return tokens
- [ ] Report endpoints work
- [ ] Admin endpoints are protected
- [ ] No "environment variable not found" errors in logs

---

## Troubleshooting Environment Variables

### Problem: "DATABASE_URL not found" error

**Solution**:
1. Verify exact spelling: `DATABASE_URL` (case-sensitive)
2. Check value starts with `postgresql://`
3. Ensure there are no empty spaces
4. Redeploy after adding/fixing variable

### Problem: "Invalid DATABASE_URL format" error

**Check**:
- Format must be: `postgresql://user:pass@host:port/db`
- No spaces in the URL
- Special characters in password are URL-encoded if needed
- Port number is numeric (usually 5432)

### Problem: "JWT_SECRET too short" error

**Solution**:
- Minimum 32 characters required
- Generate new secret with at least 32 chars
- Test locally before deploying

### Problem: CORS errors in frontend

**Solution**:
1. Check `CORS_ALLOWED_ORIGINS` is set correctly
2. Ensure domain matches exactly (including `www.` if used)
3. Do NOT include `/api` in the origin
4. Separate multiple domains with commas (no spaces)
5. Redeploy after changes

### Problem: Deployment stuck/failed

**Check logs**:
1. Render Dashboard → Web Service
2. Click "Logs" tab
3. Look for specific error message
4. Most common: Missing or invalid environment variable
5. Fix and redeploy

---

## Security Best Practices

### JWT_SECRET

✅ **DO**:
- Generate cryptographically strong random string
- Store securely in Render environment (NOT in code)
- Use 32+ characters
- Rotate periodically (quarterly)
- Keep it secret!

❌ **DON'T**:
- Use simple/predictable secrets
- Store in application.properties file
- Share in emails or chat
- Reuse across environments
- Commit to Git repository

### DATABASE_URL

✅ **DO**:
- Copy from Render PostgreSQL details
- Use Render's managed PostgreSQL
- Enable automatic backups
- Monitor connection usage

❌ **DON'T**:
- Hardcode credentials in code
- Use weak passwords
- Share database URL publicly
- Allow unrestricted access from outside Render

### CORS_ALLOWED_ORIGINS

✅ **DO**:
- Use HTTPS in production
- List only trusted domains
- Update when frontend domain changes

❌ **DON'T**:
- Use `*` (allows all)
- Include protocols like `http://` in production
- Use localhost in production
- Allow untrusted origins

---

## Environment Variables Summary Table

| Variable | Required | Default | Category | Notes |
|----------|----------|---------|----------|-------|
| DATABASE_URL | ✅ YES | None | Database | PostgreSQL connection string |
| JWT_SECRET | ✅ YES | None | Security | Min 32 chars, cryptographically random |
| PORT | ❌ No | 8080 | Server | Render assigns port automatically |
| CORS_ALLOWED_ORIGINS | ❌ No | localhost:3000/4200 | Security | Update with frontend domain |

---

## Quick Copy-Paste Template

### For Render Dashboard

```
DATABASE_URL: 
postgresql://postgres:YOUR_PASSWORD@YOUR_HOST:5432/trinetra

JWT_SECRET:
your-generated-32-plus-character-secret-key-here

CORS_ALLOWED_ORIGINS:
https://yourdomain.com,https://www.yourdomain.com

PORT:
8080
```

---

## Common Configurations

### Development Environment

```
DATABASE_URL: postgresql://postgres:password@localhost:5432/trinetra
JWT_SECRET: dev-secret-key-min-32-chars-1234567890
CORS_ALLOWED_ORIGINS: http://localhost:3000,http://localhost:4200
PORT: 8080
```

### Production on Render

```
DATABASE_URL: postgresql://postgres:strong_password@dpg-xxxxx.render.com:5432/trinetra
JWT_SECRET: cryptographically-random-32-plus-char-secret-key
CORS_ALLOWED_ORIGINS: https://yourdomain.com,https://www.yourdomain.com
PORT: 8080
```

---

## Testing Environment Variables Locally

Before deploying to Render, test locally:

### PowerShell

```powershell
$env:DATABASE_URL = "postgresql://user:pass@localhost:5432/trinetra"
$env:JWT_SECRET = "your-secret-key"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:3000"
$env:PORT = "8081"

cd demo
mvn spring-boot:run
```

### Bash

```bash
export DATABASE_URL="postgresql://user:pass@localhost:5432/trinetra"
export JWT_SECRET="your-secret-key"
export CORS_ALLOWED_ORIGINS="http://localhost:3000"
export PORT="8081"

cd demo
mvn spring-boot:run
```

---

**Last Updated**: March 6, 2024  
**Version**: 1.0.0  
**Status**: Production Ready
