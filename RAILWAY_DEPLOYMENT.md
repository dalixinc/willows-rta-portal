# Railway Deployment Guide

Complete guide for deploying to Railway with PostgreSQL.

---

## 🎯 Overview

Railway deployment is **different** from local development:

**Local:** You choose profiles manually  
**Railway:** Uses environment variables automatically

**Don't worry - I'll set it up so it works seamlessly!** ✅

---

## 🚀 Railway Setup (Step-by-Step)

### Step 1: Add PostgreSQL to Railway Project

**In Railway Dashboard:**

1. Go to your project: https://railway.app/dashboard
2. Click **"New"** → **"Database"** → **"Add PostgreSQL"**
3. PostgreSQL service automatically added ✅
4. Railway automatically creates these environment variables:
   - `DATABASE_URL` (full connection string)
   - `PGHOST`
   - `PGPORT`
   - `PGUSER`
   - `PGPASSWORD`
   - `PGDATABASE`

---

### Step 2: Configure Spring Boot for Railway

We need to add a **production profile** that reads Railway's environment variables.

**Add this to your `application.properties`:**

```properties
# ====================================================================================
# RAILWAY PRODUCTION - PROFILE: production
# This profile is automatically activated on Railway
# Uses Railway's environment variables
# ====================================================================================

---
spring.config.activate.on-profile: production

# PostgreSQL Database (from Railway environment variables)
spring.datasource.url: ${DATABASE_URL}
spring.datasource.driver-class-name: org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform: org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto: update
spring.jpa.show-sql: false

# Security - disable H2 console in production
spring.h2.console.enabled: false

# Disable DevTools in production
spring.devtools.restart.enabled: false
```

---

### Step 3: Set Railway Environment Variable

**In Railway Dashboard:**

1. Click on your **Spring Boot service** (not the database)
2. Go to **"Variables"** tab
3. Click **"New Variable"**
4. Add:
   ```
   Name: SPRING_PROFILES_ACTIVE
   Value: production
   ```
5. Click **"Add"**

**This tells Railway to use the production profile automatically!** ✅

---

### Step 4: Deploy

**Push to GitHub:**

```bash
git add .
git commit -m "Add Railway production profile"
git push origin main
```

**Railway will:**
1. Detect the push
2. Build your app
3. Set `SPRING_PROFILES_ACTIVE=production`
4. Connect to PostgreSQL using `DATABASE_URL`
5. Deploy! 🚀

---

## 🔍 How It Works

### Local Development (Your Machine)

```bash
# You choose explicitly:
mvn spring-boot:run -Dspring-boot.run.profiles=h2-update

# Or:
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-create
```

**Uses:** Whatever profile you specify

---

### Railway Production

```bash
# Railway automatically sets:
SPRING_PROFILES_ACTIVE=production

# Spring Boot uses the 'production' profile
# Which reads DATABASE_URL from environment
```

**Uses:** `production` profile (PostgreSQL from Railway)

---

## ✅ Complete application.properties for Railway

Here's what your **FULL** `application.properties` should look like:

```properties
# ====================================================================================
# COMMON SETTINGS
# ====================================================================================

spring.application.name=Willows RTA Portal
server.port=8082
spring.thymeleaf.cache=false
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.mail.from=noreply@willowsrta.org
app.email.enabled=false
app.mfa.enabled=true
app.self-registration.enabled=true

# ====================================================================================
# H2 - LOCAL DEVELOPMENT
# ====================================================================================

---
spring.config.activate.on-profile: h2-create
spring.datasource.url: jdbc:h2:file:./data/willowsdb;CIPHER=AES
spring.datasource.driver-class-name: org.h2.Driver
spring.datasource.username: sa
spring.datasource.password: filepassword WillowsRTA2026!Secure
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto: create
spring.jpa.show-sql: true
spring.h2.console.enabled: true
spring.h2.console.path: /h2-console

---
spring.config.activate.on-profile: h2-update
spring.datasource.url: jdbc:h2:file:./data/willowsdb;CIPHER=AES
spring.datasource.driver-class-name: org.h2.Driver
spring.datasource.username: sa
spring.datasource.password: filepassword WillowsRTA2026!Secure
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto: update
spring.jpa.show-sql: true
spring.h2.console.enabled: true
spring.h2.console.path: /h2-console

# ====================================================================================
# POSTGRESQL - LOCAL DEVELOPMENT
# ====================================================================================

---
spring.config.activate.on-profile: postgres-create
spring.datasource.url: jdbc:postgresql://localhost:5432/willowsrta
spring.datasource.username: postgres
spring.datasource.password: postgres
spring.datasource.driver-class-name: org.postgresql.Driver
spring.jpa.database-platform: org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto: create
spring.jpa.show-sql: true
spring.h2.console.enabled: false

---
spring.config.activate.on-profile: postgres-update
spring.datasource.url: jdbc:postgresql://localhost:5432/willowsrta
spring.datasource.username: postgres
spring.datasource.password: postgres
spring.datasource.driver-class-name: org.postgresql.Driver
spring.jpa.database-platform: org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto: update
spring.jpa.show-sql: true
spring.h2.console.enabled: false

# ====================================================================================
# RAILWAY PRODUCTION
# ====================================================================================

---
spring.config.activate.on-profile: production

# PostgreSQL from Railway (uses DATABASE_URL environment variable)
spring.datasource.url: ${DATABASE_URL}
spring.datasource.driver-class-name: org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform: org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto: update
spring.jpa.show-sql: false

# Production settings
spring.h2.console.enabled: false
spring.devtools.restart.enabled: false
```

---

## 🔧 Railway Environment Variables to Set

**In Railway Dashboard → Your Service → Variables:**

| Variable | Value | Why |
|----------|-------|-----|
| `SPRING_PROFILES_ACTIVE` | `production` | Tells Spring to use production profile |
| `DATABASE_URL` | (auto-set by Railway) | Connection to PostgreSQL |

**That's it!** Railway sets `DATABASE_URL` automatically when you add PostgreSQL.

---

## 🧪 Testing Railway Deployment

### Before Deploying:

**1. Test locally with PostgreSQL first:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-update
```

Make sure everything works!

---

### Deploy to Railway:

**1. Add production profile to `application.properties`** (see above)

**2. Commit and push:**

```bash
git add src/main/resources/application.properties
git commit -m "Add Railway production profile"
git push origin main
```

**3. Set environment variable in Railway:**
- Variable: `SPRING_PROFILES_ACTIVE`
- Value: `production`

**4. Watch Railway logs:**

In Railway dashboard, click on your service, go to "Deployments" tab, click latest deployment.

**Look for:**
```
Started WillowsRtaPortalApplication
The following 1 profile is active: "production"
Hibernate: create table members (...)
Default admin user created
```

✅ **Success!**

---

## 📊 Profile Summary

| Environment | Profile | How Set |
|-------------|---------|---------|
| **Local H2** | `h2-update` | `-Dspring-boot.run.profiles=h2-update` |
| **Local PostgreSQL** | `postgres-update` | `-Dspring-boot.run.profiles=postgres-update` |
| **Railway** | `production` | `SPRING_PROFILES_ACTIVE=production` (env var) |

---

## 🐛 Troubleshooting Railway

### Build fails with "No active profile set"

**Solution:** Set `SPRING_PROFILES_ACTIVE=production` in Railway variables

---

### "Cannot load driver class: org.postgresql.Driver"

**Solution:** Make sure `pom.xml` has PostgreSQL dependency:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### App starts but can't connect to database

**Check Railway logs for:**
```
Connection refused
```

**Solution:** 
1. Make sure PostgreSQL service is running in Railway
2. Check `DATABASE_URL` is set in environment variables
3. Both services (app + database) should be in same project

---

### DATABASE_URL format issues

Railway's `DATABASE_URL` might look like:
```
postgresql://postgres:password@hostname:5432/railway
```

Spring Boot reads this automatically with `${DATABASE_URL}` ✅

---

## ✅ Deployment Checklist

Before deploying to Railway:

- [ ] PostgreSQL dependency in `pom.xml`
- [ ] Production profile added to `application.properties`
- [ ] PostgreSQL service added in Railway
- [ ] `SPRING_PROFILES_ACTIVE=production` set in Railway
- [ ] Code pushed to GitHub
- [ ] Check Railway deployment logs
- [ ] Test login at Railway URL
- [ ] Verify admin account works
- [ ] Check members list

---

## 🎯 Summary

**Local Development:**
```bash
# You control the profile manually
mvn spring-boot:run -Dspring-boot.run.profiles=h2-update
```

**Railway Production:**
```
# Railway sets it automatically via environment variable
SPRING_PROFILES_ACTIVE=production
```

**Both work independently - no conflicts!** ✅

---

**Ready to deploy!** 🚀
