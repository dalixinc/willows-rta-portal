# Simple H2 to PostgreSQL Migration Workflow

Easy step-by-step guide to test migrating from H2 to PostgreSQL.

---

## 🎯 What You'll Do

1. Start with fresh H2 database
2. Add some test data (members, users)
3. Export H2 data
4. Import to PostgreSQL
5. Verify it worked

---

## 📋 Prerequisites

### PostgreSQL Setup

**1. Check PostgreSQL is running:**

```bash
# Windows
# Check Services for "postgresql" - should be running

# Mac
brew services list | grep postgresql

# Linux
sudo systemctl status postgresql
```

**2. Drop existing database (start fresh):**

```bash
psql -U postgres

# You'll be prompted for password, then:
DROP DATABASE IF EXISTS willowsrta;
CREATE DATABASE willowsrta;

# Verify it's empty
\c willowsrta
\dt
# Should show: "Did not find any relations."

# Exit
\q
```

---

## 🚀 Step-by-Step Migration

### Phase 1: Populate H2 Database

**1. Delete existing H2 data (start fresh):**

```bash
cd willows-rta-portal

# Delete H2 database files
rm -rf data/

# Or on Windows:
# rmdir /s data
```

**2. Start app with H2 (create mode):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2-create
```

**Wait for:**
```
Started WillowsRtaPortalApplication
Default admin user created:
Username: admin
Password: admin123
```

**3. Add test data:**

Open browser: http://localhost:8082

- Login as admin/admin123
- Add 2-3 members manually (Admin → Add Member)
- Create login accounts for some members
- Maybe promote one to admin role
- Test everything works

**4. Stop the app:**

Press `Ctrl+C`

---

### Phase 2: Export from H2

**1. Start app (update mode this time):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2-update
```

**2. Open H2 Console:**

Browser: http://localhost:8082/h2-console

Login:
```
JDBC URL: jdbc:h2:file:./data/willowsdb;CIPHER=AES
User Name: sa
Password: filepassword WillowsRTA2026!Secure
```

**3. Export data:**

Run this SQL:
```sql
SCRIPT TO 'h2_export.sql';
```

**4. Verify export file created:**

```bash
ls -lh h2_export.sql
# Should see file with size (not 0 bytes)
```

**5. Stop the app:**

Press `Ctrl+C`

---

### Phase 3: Prepare PostgreSQL Import Script

**1. Convert H2 export to PostgreSQL format:**

I'll create a script that does this automatically. For now, manually create:

**File: `postgres_import.sql`**

```sql
-- Clear any existing data
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE members CASCADE;
TRUNCATE TABLE otp_codes CASCADE;

-- Copy INSERT statements from h2_export.sql here
-- Change IS_LEASEHOLDER to leaseholder
-- Remove PUBLIC. prefix
-- Keep the data as-is

-- Then add at the end:
SELECT setval('members_id_seq', (SELECT MAX(id) FROM members));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('otp_codes_id_seq', COALESCE((SELECT MAX(id) FROM otp_codes), 1));
```

---

### Phase 4: Import to PostgreSQL

**1. Start app with PostgreSQL (create mode):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-create
```

**Wait for:**
```
Started WillowsRtaPortalApplication
Default admin user created
```

**This creates empty tables in PostgreSQL**

**2. Stop the app:**

Press `Ctrl+C`

**3. Import your data:**

```bash
psql -U postgres -d willowsrta -f postgres_import.sql
```

**4. Verify import:**

```bash
psql -U postgres -d willowsrta

SELECT COUNT(*) FROM members;
SELECT COUNT(*) FROM users;

SELECT full_name, email FROM members;
SELECT username, role FROM users;

\q
```

**5. Start app (update mode):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-update
```

**6. Test:**

- Open: http://localhost:8082
- Login with admin/admin123
- Check members list
- Verify all your test data is there!

---

## ✅ Success Checklist

After migration:

- [ ] PostgreSQL database exists
- [ ] Tables created (members, users, otp_codes)
- [ ] Data imported correctly
- [ ] Can login with admin account
- [ ] Members list shows all members
- [ ] User accounts work
- [ ] Relationships intact (member ↔ user links)

---

## 🔧 PostgreSQL Management Commands

### Useful psql commands:

```bash
# Connect to database
psql -U postgres -d willowsrta

# List databases
\l

# List tables
\dt

# Describe table structure
\d members
\d users

# View data
SELECT * FROM members;

# Count records
SELECT COUNT(*) FROM members;

# Drop database (start over)
DROP DATABASE willowsrta;
CREATE DATABASE willowsrta;

# Exit
\q
```

### Using pgAdmin (GUI):

**Drop database:**
1. Open pgAdmin
2. Expand Servers → PostgreSQL
3. Right-click "willowsrta" database
4. Delete/Drop
5. Confirm

**Create database:**
1. Right-click "Databases"
2. Create → Database
3. Name: willowsrta
4. Save

**View tables:**
1. Expand: willowsrta → Schemas → public → Tables
2. Right-click table → View/Edit Data → All Rows

---

## 🎓 Profile Reference

### H2 Modes:

```bash
# Fresh start (drops/recreates tables)
mvn spring-boot:run -Dspring-boot.run.profiles=h2-create

# Continue working (keeps data)
mvn spring-boot:run -Dspring-boot.run.profiles=h2-update

# Or just (uses h2-update by default)
mvn spring-boot:run
```

### PostgreSQL Modes:

```bash
# Fresh start (drops/recreates tables)
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-create

# Continue working (keeps data)
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-update

# Import mode (for migration)
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-import
```

---

## 🐛 Troubleshooting

### "database willowsrta already exists"

```bash
psql -U postgres
DROP DATABASE willowsrta;
CREATE DATABASE willowsrta;
\q
```

### "relation does not exist" when importing

You forgot to start the app first! The app creates tables.

```bash
# Do this BEFORE importing:
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-create
# Wait for startup
# Ctrl+C to stop
# THEN import data
```

### H2 Console won't open

Make sure you're using h2-create or h2-update profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2-update
```

### PostgreSQL won't connect

Check it's running:
```bash
# Mac
brew services start postgresql

# Linux
sudo systemctl start postgresql

# Windows
# Start Services → postgresql service
```

Check password in application.properties matches your postgres password.

---

## 📝 Quick Migration Summary

```bash
# 1. Fresh H2
rm -rf data/
mvn spring-boot:run -Dspring-boot.run.profiles=h2-create
# Add test data
# Ctrl+C

# 2. Export H2
mvn spring-boot:run -Dspring-boot.run.profiles=h2-update
# Open H2 Console
# Run: SCRIPT TO 'h2_export.sql';
# Ctrl+C

# 3. Prepare PostgreSQL
psql -U postgres
DROP DATABASE IF EXISTS willowsrta;
CREATE DATABASE willowsrta;
\q

# 4. Create tables
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-create
# Wait for startup
# Ctrl+C

# 5. Import data
psql -U postgres -d willowsrta -f postgres_import.sql

# 6. Test
mvn spring-boot:run -Dspring-boot.run.profiles=postgres-update
# Login and verify!
```

---

**You're all set!** Follow these steps and you'll successfully migrate from H2 to PostgreSQL. 🎉
