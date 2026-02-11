# PostgreSQL Migration Guide

Complete guide to migrating from H2 to PostgreSQL for both local development and Railway deployment.

---

## 🎯 Why PostgreSQL?

**Benefits over H2 file-based:**
- ✅ No more database file corruption
- ✅ MFA toggle works without database reset
- ✅ Better concurrent access
- ✅ Production-grade reliability
- ✅ Better schema migration handling
- ✅ Easier backups and restore
- ✅ Industry standard for production

---

## 📦 Prerequisites

### Local Development
- PostgreSQL 12 or higher installed
- pgAdmin (optional, for GUI management)
- Existing H2 database with data (optional - for migration)

### Railway Deployment
- Railway account
- GitHub repository
- Railway CLI (optional but recommended)

---

## 🔧 Part 1: Local PostgreSQL Setup

### Step 1: Install PostgreSQL

**Windows:**
1. Download from https://www.postgresql.org/download/windows/
2. Run installer
3. Set password for postgres user (remember this!)
4. Default port: 5432
5. Install pgAdmin when prompted

**Mac (Homebrew):**
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### Step 2: Create Database and User

**Option A: Using pgAdmin (GUI)**
1. Open pgAdmin
2. Right-click "Databases" → Create → Database
3. Name: `willowsrta`
4. Owner: postgres
5. Save

**Option B: Using psql (Command Line)**
```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE willowsrta;

# Create user (optional - use postgres or create new)
CREATE USER willowsadmin WITH PASSWORD 'your-secure-password';

# Grant privileges
GRANT ALL PRIVILEGES ON DATABASE willowsrta TO willowsadmin;

# Exit
\q
```

### Step 3: Update pom.xml

Add PostgreSQL dependency:

```xml
<!-- In <dependencies> section, add: -->

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Remove or comment out H2 dependency:**
```xml
<!-- H2 Database (REMOVE OR COMMENT OUT)
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
-->
```

### Step 4: Update application.properties

**BACKUP YOUR CURRENT FILE FIRST!**

Replace H2 configuration with PostgreSQL:

```properties
# Application Configuration
spring.application.name=Willows RTA Portal

# Server Configuration
server.port=8082

# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/willowsrta
spring.datasource.username=postgres
spring.datasource.password=your-postgres-password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Connection pool settings (optional but recommended)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# Thymeleaf Configuration
spring.thymeleaf.cache=false

# File upload settings
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Email Configuration (for OTP/MFA)
spring.mail.from=noreply@willowsrta.org

# MFA/OTP Configuration
app.mfa.enabled=true

# Self-Registration Configuration
app.self-registration.enabled=true

# Email Provider Configuration
# (Keep your existing email settings)
app.email.enabled=false

# DEVELOPMENT MODE: When email is disabled, OTP codes will be printed to console
```

### Step 5: First Run (Clean Database)

```bash
# Clean any previous build
mvn clean

# Build the project
mvn package

# Run the application
mvn spring-boot:run
```

**What happens:**
- PostgreSQL database created
- Tables auto-created by Hibernate
- Default admin user created (admin/admin123)
- Application starts on port 8082

**Check it worked:**
```
✅ Application starts without errors
✅ Can login with admin/admin123
✅ Can create test member
✅ Can toggle MFA without database reset!
```

### Step 6: Verify Database

**Using pgAdmin:**
1. Open pgAdmin
2. Navigate to: Servers → PostgreSQL → Databases → willowsrta → Schemas → public → Tables
3. Should see: members, users, otp_codes

**Using psql:**
```bash
psql -U postgres -d willowsrta

# List tables
\dt

# View users table
SELECT * FROM users;

# Exit
\q
```

---

## 💾 Part 2: Migrating Existing H2 Data (Optional)

If you have existing members/users in H2 that you want to keep:

### Option 1: Manual Export/Import (Recommended for Small Datasets)

**Step 1: Export from H2**
```bash
# While H2 is still configured, run app
mvn spring-boot:run

# Access H2 Console
http://localhost:8082/h2-console

# Run these queries and save results as CSV:

-- Export members
SELECT * FROM members;
-- Save as members.csv

-- Export users
SELECT * FROM users;
-- Save as users.csv
```

**Step 2: Import to PostgreSQL**

Using pgAdmin:
1. Right-click table → Import/Export
2. Select Import
3. Choose CSV file
4. Map columns
5. Import

Using psql:
```bash
# Copy CSV files to a location PostgreSQL can access
# Then:
psql -U postgres -d willowsrta

\copy members FROM '/path/to/members.csv' CSV HEADER;
\copy users FROM '/path/to/users.csv' CSV HEADER;
```

**Step 3: Reset Sequences**
```sql
-- Fix auto-increment counters
SELECT setval('members_id_seq', (SELECT MAX(id) FROM members));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
```

### Option 2: Start Fresh (Easiest)

Just start with clean PostgreSQL database:
1. Default admin account created automatically
2. Members re-register OR admin adds them manually
3. Clean slate, no legacy issues

---

## ☁️ Part 3: Railway Deployment

### Option 1: Railway Dashboard (Easiest)

**Step 1: Add PostgreSQL to Railway Project**

1. Go to Railway dashboard: https://railway.app
2. Open your project
3. Click "New" → "Database" → "Add PostgreSQL"
4. PostgreSQL service added automatically
5. Note the connection details

**Step 2: Get Database Credentials**

1. Click on PostgreSQL service
2. Go to "Variables" tab
3. Copy these variables:
   - `DATABASE_URL` (full connection string)
   - `PGDATABASE`
   - `PGHOST`
   - `PGPASSWORD`
   - `PGPORT`
   - `PGUSER`

**Step 3: Update Your Application**

In your GitHub repository:

1. **Update `pom.xml`** (add PostgreSQL, remove H2)
2. **Update `application.properties`:**

```properties
# Use Railway environment variables
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# All other settings stay the same
```

**Step 4: Deploy**

```bash
git add .
git commit -m "Migrate to PostgreSQL"
git push origin main
```

Railway will:
- Detect changes
- Auto-deploy
- Connect to PostgreSQL
- Create tables
- Start application

**Step 5: Verify**

1. Check Railway logs for successful startup
2. Visit your Railway URL
3. Login with admin/admin123
4. Test creating a member

### Option 2: Railway CLI

**Step 1: Install Railway CLI**

```bash
# Mac/Linux
curl -fsSL https://railway.app/install.sh | sh

# Windows (PowerShell)
iwr https://railway.app/install.ps1 | iex
```

**Step 2: Login and Link**

```bash
railway login
cd willows-rta-portal
railway link
```

**Step 3: Add PostgreSQL**

```bash
railway add postgresql
```

**Step 4: Deploy**

```bash
railway up
```

**Step 5: View Logs**

```bash
railway logs
```

---

## 🔍 Part 4: Verification Checklist

### Local PostgreSQL

- [ ] PostgreSQL service running
- [ ] Database `willowsrta` created
- [ ] Application starts without errors
- [ ] Can login with admin/admin123
- [ ] Can create test member
- [ ] Can create login account for member
- [ ] MFA toggle works without database reset
- [ ] No "database is read-only" errors

### Railway PostgreSQL

- [ ] PostgreSQL service added to project
- [ ] Environment variables configured
- [ ] Application deployed successfully
- [ ] No deployment errors in logs
- [ ] Can access via Railway URL
- [ ] Can login with admin/admin123
- [ ] All features work correctly
- [ ] Database persists across deployments

---

## 🐛 Troubleshooting

### Local Issues

**Error: "connection refused"**
```
Solution: Make sure PostgreSQL is running
Windows: Check Services
Mac: brew services list
Linux: sudo systemctl status postgresql
```

**Error: "password authentication failed"**
```
Solution: Check password in application.properties matches postgres password
```

**Error: "database does not exist"**
```
Solution: Create database first
psql -U postgres
CREATE DATABASE willowsrta;
```

**Tables not created**
```
Solution: Check spring.jpa.hibernate.ddl-auto=update
Check logs for Hibernate errors
```

### Railway Issues

**Build fails**
```
Solution: Check pom.xml has PostgreSQL dependency
Check no syntax errors in application.properties
```

**Can't connect to database**
```
Solution: Check DATABASE_URL variable is set
Check PostgreSQL service is running in Railway
```

**Application crashes on startup**
```
Solution: Check Railway logs
Verify environment variables are correct
Check for missing dependencies
```

**Old H2 data visible**
```
Solution: Railway might be caching
Delete old deployments
Redeploy fresh
```

---

## 🔄 Rollback Plan

If PostgreSQL migration fails:

**Local:**
1. Restore `application.properties` from backup
2. Restore `pom.xml` from backup
3. Run `mvn clean package`
4. Run `mvn spring-boot:run`
5. Back to H2

**Railway:**
1. Revert GitHub commit: `git revert HEAD`
2. Push: `git push origin main`
3. Railway auto-deploys old version
4. Back to H2

---

## 📊 Configuration Comparison

| Setting | H2 (Old) | PostgreSQL (New) |
|---------|----------|------------------|
| **Driver** | `org.h2.Driver` | `org.postgresql.Driver` |
| **URL** | `jdbc:h2:file:./data/willowsdb` | `jdbc:postgresql://localhost:5432/willowsrta` |
| **Dialect** | `H2Dialect` | `PostgreSQLDialect` |
| **Data Location** | `./data/` folder | PostgreSQL data directory |
| **Encryption** | File-level (AES) | Database-level (configurable) |
| **Concurrent Users** | Limited | Excellent |
| **MFA Toggle** | Requires DB reset | Works seamlessly |
| **Production Ready** | No | Yes |

---

## 🎯 Post-Migration Tasks

### Local

1. **Test all features thoroughly**
2. **Set up regular backups:**
   ```bash
   pg_dump -U postgres willowsrta > backup.sql
   ```
3. **Secure PostgreSQL:**
   - Change default postgres password
   - Configure pg_hba.conf for access control
4. **Monitor performance**

### Railway

1. **Test all features on deployed app**
2. **Set up automated backups** (Railway Pro feature)
3. **Configure environment for production:**
   ```properties
   spring.jpa.show-sql=false
   spring.jpa.hibernate.ddl-auto=validate
   ```
4. **Set up monitoring/alerts**
5. **Configure custom domain** (optional)

---

## 📝 Production Checklist

Before going live with PostgreSQL:

- [ ] Backup existing data (if any)
- [ ] Test migration in development first
- [ ] Update all configuration files
- [ ] Test all features work
- [ ] Change default admin password
- [ ] Configure email provider
- [ ] Set MFA to desired state
- [ ] Set self-registration to desired state
- [ ] Disable SQL logging (`spring.jpa.show-sql=false`)
- [ ] Set DDL to validate (`spring.jpa.hibernate.ddl-auto=validate`)
- [ ] Test member registration
- [ ] Test admin functions
- [ ] Test account security (lock/unlock, password reset)
- [ ] Test role management
- [ ] Verify backups working
- [ ] Document admin procedures
- [ ] Train admin users

---

## 🆘 Support & Resources

**PostgreSQL Documentation:**
- Official Docs: https://www.postgresql.org/docs/
- Spring Data JPA: https://spring.io/guides/gs/accessing-data-jpa/

**Railway Documentation:**
- Railway Docs: https://docs.railway.app/
- PostgreSQL Plugin: https://docs.railway.app/databases/postgresql

**Troubleshooting:**
- Check application logs
- Check PostgreSQL logs
- Check Railway deployment logs
- Review this guide's troubleshooting section

---

## 🎉 Success!

Once migrated successfully:
- ✅ No more H2 file corruption issues
- ✅ MFA toggle works without database reset
- ✅ Production-ready database
- ✅ Better performance
- ✅ Professional deployment
- ✅ Easier backups
- ✅ Industry-standard setup

**Welcome to PostgreSQL!** 🐘

---

**Version:** 1.0  
**Last Updated:** February 2026  
**Tested With:** PostgreSQL 14, Railway PostgreSQL Plugin
