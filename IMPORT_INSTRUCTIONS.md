# PostgreSQL Import Instructions

Your H2 data is ready to import! Here's exactly how to do it.

---

## 📊 Your Data Summary

**From your H2 export:**
- ✅ **4 Members** (Goig, Dale Macdonald, bob, Goig)
- ✅ **3 Users** (admin, dale_macdonald@hotmail.com, dork2@gmail.com)
- ✅ **Relationships** preserved
- ✅ **Passwords** preserved (encrypted)
- ✅ **System admin** flag preserved

---

## 🚀 Step-by-Step Import Process

### Phase 1: Setup PostgreSQL (If not done yet)

**1. Install PostgreSQL**
- Download from https://www.postgresql.org/download/
- Install with default settings
- Remember the password you set for `postgres` user!

**2. Create Database**

Using psql:
```bash
psql -U postgres

CREATE DATABASE willowsrta;

\q
```

Or using pgAdmin:
- Right-click "Databases" → Create → Database
- Name: `willowsrta`
- Save

---

### Phase 2: Update Application Configuration

**1. Update `pom.xml`**

Add PostgreSQL dependency:
```xml
<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Comment out H2:
```xml
<!-- H2 Database
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
-->
```

**2. Update `application.properties`**

Replace H2 configuration with:
```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/willowsrta
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD_HERE
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Remove H2 Console settings (no longer needed)
# spring.h2.console.enabled=false

# Keep all other settings (email, MFA, etc.) the same
```

---

### Phase 3: Create Empty Tables

**1. Start application ONCE to create tables:**

```bash
mvn clean package
mvn spring-boot:run
```

**Wait for:**
```
Started WillowsRtaPortalApplication in X seconds
```

**You'll see in logs:**
```
Hibernate: create table members (...)
Hibernate: create table users (...)
Hibernate: create table otp_codes (...)
Default admin user created:
Username: admin
Password: admin123
```

**2. Stop the application:**
- Press `Ctrl+C`

**Note:** The new default admin (admin/admin123) will be replaced by your imported data in the next step.

---

### Phase 4: Import Your Data

**Method A: Using psql (Command Line)**

```bash
# Navigate to your project folder
cd willows-rta-portal

# Import the data
psql -U postgres -d willowsrta -f import_to_postgresql.sql
```

**Expected output:**
```
TRUNCATE TABLE
TRUNCATE TABLE
TRUNCATE TABLE
ALTER SEQUENCE
ALTER SEQUENCE
ALTER SEQUENCE
INSERT 0 4
INSERT 0 3
SELECT 1
SELECT 1
SELECT 1
 ?column?          
-------------------
 Members imported: 4
(1 row)

 ?column?          
-------------------
 Users imported: 3
(1 row)

...

        status        
----------------------
 Import completed successfully!
```

**Method B: Using pgAdmin (GUI)**

1. Open pgAdmin
2. Navigate to: Servers → PostgreSQL → Databases → willowsrta
3. Right-click `willowsrta` → Query Tool
4. File → Open → Select `import_to_postgresql.sql`
5. Click Execute (⚡ lightning bolt icon)
6. Check output panel for success messages

---

### Phase 5: Verify Import

**Run these queries in psql or pgAdmin:**

```sql
-- Count records
SELECT COUNT(*) FROM members;  -- Should be 4
SELECT COUNT(*) FROM users;    -- Should be 3

-- Check members
SELECT id, full_name, email, has_user_account 
FROM members 
ORDER BY id;

-- Check users
SELECT id, username, role, system_admin 
FROM users 
ORDER BY id;

-- Verify relationships
SELECT m.full_name, u.username, u.role
FROM members m
LEFT JOIN users u ON u.member_id = m.id
WHERE m.has_user_account = true;
```

**Expected Results:**

**Members:**
```
 id |    full_name    |           email           | has_user_account
----+-----------------+---------------------------+------------------
  1 | Goig            | dork@gmail.com            | f
  2 | Dale Macdonald  | dale_macdonald@hotmail.com| t
  3 | bob             | bob@a.com                 | f
  4 | Goig            | dork2@gmail.com           | t
```

**Users:**
```
 id |         username          |    role     | system_admin
----+---------------------------+-------------+--------------
  1 | admin                     | ROLE_ADMIN  | t
  2 | dale_macdonald@hotmail.com| ROLE_MEMBER | f
  3 | dork2@gmail.com           | ROLE_ADMIN  | f
```

---

### Phase 6: Start Application

```bash
mvn spring-boot:run
```

**Check logs for:**
```
Started WillowsRtaPortalApplication
```

**Should NOT see:**
```
Default admin user created
```
(Because admin already exists from import!)

---

### Phase 7: Test Login

**1. Login with imported admin account:**
```
Username: admin
Password: admin123
```

**Should work!** ✅

**2. Check members list:**
- Navigate to Admin → Members
- Should see 4 members
- Dale Macdonald and Goig (dork2) should show "Has Login: ✓"

**3. Test other accounts:**

**Dale Macdonald:**
- Username: dale_macdonald@hotmail.com
- Should require password change (password_change_required = TRUE)

**Goig (dork2):**
- Username: dork2@gmail.com
- Role: Admin (promoted user)
- Should require password change

---

## ✅ Success Checklist

After import, verify:

- [x] PostgreSQL running
- [x] Database `willowsrta` exists
- [x] Tables created (members, users, otp_codes)
- [x] 4 members imported
- [x] 3 users imported
- [x] Admin account works (admin/admin123)
- [x] Member-User relationships intact
- [x] Dale Macdonald has login account
- [x] Goig (dork2@gmail.com) has admin role
- [x] System admin flag set on admin account
- [x] Application starts without errors
- [x] Can view members list
- [x] Can view member details

---

## 🐛 Troubleshooting

### "relation does not exist"

**Problem:** Tables not created yet

**Solution:**
```bash
# Start app once to create tables
mvn spring-boot:run
# Wait for startup, then Ctrl+C
# Then import data
```

### "duplicate key value violates unique constraint"

**Problem:** Trying to import twice

**Solution:**
```sql
-- Clear all data first
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE members CASCADE;
TRUNCATE TABLE otp_codes CASCADE;

-- Then run import again
```

### "column 'leaseholder' does not exist"

**Problem:** H2 used `IS_LEASEHOLDER`, PostgreSQL expects `leaseholder`

**Solution:** The import script already handles this - just make sure you're using `import_to_postgresql.sql`, not the raw H2 export.

### Passwords don't work after import

**Problem:** Passwords are correct, just need to be changed

**Solution:** 
- Dale and Goig (dork2) have `password_change_required = TRUE`
- They'll be prompted to change password on first login
- This is expected behavior!

### Can't login with admin

**Problem:** Wrong password

**Solution:**
```
Username: admin
Password: admin123

NOT your old H2 admin password!
The import preserves the original encrypted password.
```

---

## 🔄 What Got Imported?

### Member Records:
1. **Goig** (dork@gmail.com) - No login account
2. **Dale Macdonald** (dale_macdonald@hotmail.com) - Has login, MEMBER role
3. **bob** (bob@a.com) - No login account  
4. **Goig** (dork2@gmail.com) - Has login, ADMIN role

### User Accounts:
1. **admin** - System administrator (cannot be deleted)
2. **dale_macdonald@hotmail.com** - Member role, password change required
3. **dork2@gmail.com** - Admin role, password change required

### Preserved:
- ✅ Encrypted passwords
- ✅ Member-User relationships
- ✅ System admin flag
- ✅ Registration dates
- ✅ All member details
- ✅ Role assignments

### Not Imported:
- ❌ OTP codes (expired anyway, skip them)

---

## 📝 Post-Import Tasks

### 1. Change Default Admin Password

```
Login as: admin/admin123
Navigate to: Account Settings
Change password to something secure
```

### 2. Disable H2 Console

In `application.properties`:
```properties
# Remove or comment out:
# spring.h2.console.enabled=true
# spring.h2.console.path=/h2-console
```

### 3. Disable SQL Logging (Production)

```properties
spring.jpa.show-sql=false
```

### 4. Set DDL to Validate (Production)

```properties
spring.jpa.hibernate.ddl-auto=validate
```

### 5. Remove H2 Dependency

Remove from `pom.xml`:
```xml
<!-- Delete this entire dependency block -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 6. Remove SecurityConfig H2 Exceptions

Remove from `SecurityConfig.java`:
```java
// Remove this line:
.requestMatchers("/h2-console/**").permitAll()

// Remove from CSRF:
// Remove "/h2-console/**" from ignoringRequestMatchers
```

---

## 🎉 You're Done!

Your data is now in PostgreSQL!

**Benefits you now have:**
- ✅ No more database corruption
- ✅ Can change MFA settings freely
- ✅ Production-ready database
- ✅ Better concurrent access
- ✅ Professional backup tools
- ✅ All your existing data preserved

**Next steps:**
1. Test all features thoroughly
2. Set up regular backups
3. Configure email provider
4. Deploy to Railway (if desired)

---

**Need help?** Check the troubleshooting section or see `POSTGRESQL_MIGRATION.md` for more details.
