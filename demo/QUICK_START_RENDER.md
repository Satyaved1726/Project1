# TRINETRA on Render - Quick Start Checklist

## 🚀 Deploy in 30 Minutes

Follow these steps in order to deploy TRINETRA to Render with PostgreSQL.

---

## Phase 1: Preparation (5 minutes)

### Step 1.1: Verify Local Build

```bash
cd demo
mvn clean package -DskipTests
```

✅ Should complete with: `[INFO] BUILD SUCCESS`

### Step 1.2: Create GitHub Repository

If not already done:
```bash
git add .
git commit -m "Configure for Render PostgreSQL deployment"
git push origin main
```

✅ Code should be in GitHub repository

---

## Phase 2: Render Setup (10 minutes)

### Step 2.1: Create Render Account

1. Visit: https://render.com
2. Sign up with GitHub account
3. Connect to your GitHub repository

✅ Account created and authenticated

### Step 2.2: Create PostgreSQL Database

On Render Dashboard:

1. **Click**: "New +" → "PostgreSQL"
2. **Configure**:
   - Name: `trinetra-db`
   - Database: `trinetra`
   - User: `postgres` (default)
3. **Region**: Select your region
4. **Plan**: Free (for testing)
5. **Click**: "Create Database"

⏳ Wait 2-5 minutes for initialization

✅ Database created (note the External Database URL)

### Step 2.3: Create Web Service

On Render Dashboard:

1. **Click**: "New +" → "Web Service"
2. **Connect Repository**:
   - Select your GitHub account
   - Select TRINETRA repository
   - Click "Connect"
3. **Configure**:
   - Name: `trinetra-backend`
   - Environment: `Java`
   - Region: **Same as database** (important!)
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/demo.jar`
   - Plan: Free (for testing)
4. **⚠️ DO NOT DEPLOY YET** - We need to add environment variables first

✅ Web Service created (but not deployed)

---

## Phase 3: Environment Variables (5 minutes)

### Step 3.1: Generate JWT Secret

**Option A - PowerShell**:
```powershell
-join ((1..64) | ForEach-Object {[char][int]((48..122) | Get-Random)})
```

**Option B - Online**: 
Visit https://www.random.org/strings/ and generate 32+ character random string

✅ JWT Secret generated (save it!)

### Step 3.2: Copy Database URL

1. Render Dashboard → PostgreSQL database (`trinetra-db`)
2. Copy the "External Database URL"
3. Format: `postgresql://postgres:password@host:5432/trinetra`

✅ Database URL copied

### Step 3.3: Add Environment Variables

On Render Dashboard - Web Service (`trinetra-backend`):

1. **Click**: "Environment" tab
2. **Add each variable**:

| Key | Value |
|-----|-------|
| `DATABASE_URL` | Paste from Step 3.2 |
| `JWT_SECRET` | Paste from Step 3.1 |
| `PORT` | `8080` |
| `CORS_ALLOWED_ORIGINS` | Your frontend domain (or use default) |

3. After adding each:
   - Click "Add Environment Variable"
   - Leave as-is (no sync needed)

✅ All environment variables added

---

## Phase 4: Deploy (5 minutes)

### Step 4.1: Trigger Deployment

On Web Service page (`trinetra-backend`):

1. **Click**: "Deploy" button → "Manual Deploy"
2. **Watch the logs**:
   - Look for: `[INFO] BUILD SUCCESS`
   - Look for: `Started Project1Application`

⏳ Build takes 2-3 minutes (first time)

### Step 4.2: Wait for Live Status

On Web Service page:
- Status should change from "Building" to "Live" (green indicator)

⏳ Wait for green status

✅ Application deployed!

---

## Phase 5: Verification (5 minutes)

### Step 5.1: Get Your Render URL

On Web Service page (`trinetra-backend`):
- Copy your Render URL (looks like: `https://trinetra-backend.onrender.com`)

### Step 5.2: Test Health Endpoint

```bash
# PowerShell
$url = "https://YOUR-RENDER-URL.onrender.com/api/health"
curl $url

# OR use any browser and visit:
# https://your-render-url.onrender.com/api/health
```

**Expected Response**:
```json
{
  "status": "UP",
  "service": "TRINETRA Backend",
  "database": "Connected",
  "timestamp": "2024-03-06T12:34:56"
}
```

✅ Health check passed!

### Step 5.3: Test Authentication

```bash
curl -X POST https://YOUR-RENDER-URL.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Test@12345"
  }'
```

**Expected Response** (JWT token):
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

✅ Authentication working!

### Step 5.4: Test Report Submission

```bash
curl -X POST https://YOUR-RENDER-URL.onrender.com/api/reports/submit \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Report",
    "category": "Harassment",
    "severity": "High",
    "description": "Testing the backend"
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Report submitted successfully",
  "data": {
    "token": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

✅ Report submission working!

---

## Troubleshooting

### Problem: Build Failed

**Check**: Render Logs tab
- Should see `[INFO] BUILD SUCCESS`
- If not: Check for compilation errors

**Solution**:
1. Fix any errors locally
2. Push to GitHub
3. Click "Deploy" again

### Problem: Database Connection Error

**Check**: `DATABASE_URL` environment variable
- Verify exact value from PostgreSQL details page
- Should start with `postgresql://`

**Solution**:
1. Copy-paste exactly from Render PostgreSQL page
2. Redeploy

### Problem: Health Endpoint Returns "Disconnected"

**Check**:
1. PostgreSQL database is running (status should show "Available")
2. `DATABASE_URL` environment variable is correct
3. Application logs for actual error

**Solution**:
1. Verify database URL in environment variables
2. Wait 30 seconds and refresh
3. Restart service if necessary

### Problem: CORS Error in Frontend

**Fix**:
1. Update `CORS_ALLOWED_ORIGINS` variable with your frontend domain
2. Redeploy

---

## Success Criteria

✅ **If you see all of these, you're done**:

- [x] Web Service status is "Live" (green)
- [x] Health endpoint returns database "Connected"
- [x] Auth endpoints work (get JWT token)
- [x] Report endpoints work (get tracking token)
- [x] Admin endpoints are protected (need token)
- [x] Logs show no errors

---

## Quick Reference Commands

### Local Build
```bash
cd demo
mvn clean package -DskipTests
```

### Test Health (after deployment)
```bash
curl https://your-render-url.onrender.com/api/health
```

### View Render Logs
- Render Dashboard → Web Service → Logs tab

### View PostgreSQL Details
- Render Dashboard → PostgreSQL database → Details tab

---

## Important Notes

⚠️ **Critical**:
- Set `JWT_SECRET` - do not use default
- Use same region for Web Service and Database
- Wait for database initialization (2-5 min)
- Build takes longer on first deployment (3-5 min)

✅ **Recommendations**:
- Start with Free plan for testing
- Upgrade to Starter+ for production
- Enable auto-backups for database
- Set up monitoring alerts (Render Dashboard)

---

## Next Steps After Deployment

### Connect Your Frontend

Update your frontend to use:
- API Base URL: `https://your-render-url.onrender.com`
- Endpoints: `/api/auth/signup`, `/api/reports/submit`, etc.

### Monitor Production

1. Check logs regularly: Render Dashboard → Logs
2. Monitor database usage: PostgreSQL → Stats
3. Set up alerts for errors

### Scale for Production

When ready:
1. Upgrade Web Service plan (Starter+ or higher)
2. Upgrade Database plan (Starter+ or higher)
3. Enable auto-deploy from GitHub (Settings tab)

---

## Documentation

For detailed information:
- **RENDER_DEPLOYMENT_GUIDE.md**: Complete step-by-step guide
- **RENDER_ENV_VARIABLES.md**: Environment variables reference
- **POSTGRESQL_MIGRATION_SUMMARY.md**: What changed from MySQL
- **MIGRATION_COMPLETION_REPORT.md**: Complete migration details

---

## Support

### Stuck? Check:
1. **RENDER_DEPLOYMENT_GUIDE.md** - Troubleshooting section
2. **Render Logs** - Check Web Service logs for errors
3. **Environment Variables** - Verify all are set correctly

### Common Commands:
```bash
# Test database connection locally first
export DATABASE_URL="postgresql://user:pass@host:5432/db"
export JWT_SECRET="your-secret-key"
java -jar target/demo.jar
```

---

## Success Summary

🎉 **Your TRINETRA backend is now running on Render!**

```
✅ PostgreSQL Database: Connected
✅ Web Service: Live  
✅ All endpoints: Working
✅ Security: Enabled
✅ Ready for: Production use
```

**Deployment Time**: ~30 minutes  
**Status**: Ready to serve requests  
**Next**: Connect your frontend!

---

**Quick Links**:
- Render Dashboard: https://dashboard.render.com
- TRINETRA API: https://your-render-url.onrender.com/api
- Health Check: https://your-render-url.onrender.com/api/health

---

**Date**: March 6, 2024  
**Version**: 1.0.0  
**Status**: ✅ Deployment Complete
