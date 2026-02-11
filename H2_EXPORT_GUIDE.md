# H2 Database Export Guide

Complete guide to exporting your existing H2 data before migrating to PostgreSQL.

---

## 🎯 Overview

You have two main options:

1. **Export to SQL file** (Recommended - easiest to import to PostgreSQL)
2. **Export to CSV files** (More manual, but gives you control)

---

## ✅ Method 1: Export to SQL File (RECOMMENDED)

This creates a SQL file that can be directly imported to PostgreSQL.

### Step 1: Access H2 Console

**While your app is running with H2:**

1. **Start application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Open H2 Console in browser:**
   ```
   http://localhost:8082/h2-console
   ```

3. **Login with these settings:**
   ```
   Driver Class: org.h2.Driver
   JDBC URL: jdbc:h2:file:./data/willowsdb;CIPHER=AES
   User Name: sa
   Password: filepassword WillowsRTA2026!Secure
   ```

4. **Click "Connect"**

### Step 2: Export Using SCRIPT Command

**In the SQL console, run:**

```sql
-- Export all data to SQL file
SCRIPT TO 'export.sql';
```

**This creates:** `export.sql` in your project root folder

**Contains:**
- All table structures (CREATE TABLE)
- All data (INSERT statements)
- All indexes
- All constraints

### Step 3: Verify Export File

**Check the file was created:**
```bash
# Windows
dir export.sql

# Mac/Linux
ls -lh export.sql
```

**You should see:** A file with size (e.g., 50KB, 500KB depending on data)

### Step 4: Clean Up SQL File for PostgreSQL

The H2 SQL export needs minor tweaks for PostgreSQL compatibility.

**Create a cleaned version:**

**Option A: Using text editor (find and replace):**

1. Open `export.sql` in VS Code, Notepad++, or any text editor

2. **Find and replace:**
   ```
   Find: BIGINT AUTO_INCREMENT
   Replace: BIGSERIAL

   Find: PUBLIC.
   Replace: (empty - delete it)

   Find: ENGINE=InnoDB
   Replace: (empty - delete it)
   ```

3. **Save as:** `export_postgres.sql`

**Option B: Using script (I'll create one for you below)**

### Step 5: Save for PostgreSQL Import

Your `export_postgres.sql` is ready to import to PostgreSQL!

---

## 📊 Method 2: Export to CSV Files

More manual but gives you full control over the data.

### Step 1: Access H2 Console

(Same as Method 1, Steps 1-4)

### Step 2: Export Each Table to CSV

**For Members table:**

```sql
-- Export members table
CALL CSVWRITE('members.csv', 'SELECT * FROM members');
```

**For Users table:**

```sql
-- Export users table  
CALL CSVWRITE('users.csv', 'SELECT * FROM users');
```

**For OTP codes table (if you want to keep them):**

```sql
-- Export OTP codes
CALL CSVWRITE('otp_codes.csv', 'SELECT * FROM otp_codes');
```

**Files created in project root:**
- `members.csv`
- `users.csv`
- `otp_codes.csv`

### Step 3: Verify CSV Files

**Open in Excel/Notepad to verify:**
- All columns present
- Data looks correct
- No missing rows

---

## 🔧 Method 3: Using Command Line (Advanced)

If you prefer command line:

### Export using H2's command-line tool:

```bash
# Navigate to your project directory
cd willows-rta-portal

# Export to SQL
java -cp ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar org.h2.tools.Script \
  -url "jdbc:h2:file:./data/willowsdb;CIPHER=AES" \
  -user sa \
  -password "filepassword WillowsRTA2026!Secure" \
  -script export.sql
```

*Note: Adjust H2 version number (2.2.224) if different*

---

## 🚀 Importing to PostgreSQL

### After You've Migrated to PostgreSQL:

### Option 1: Import SQL File

**Using psql command line:**

```bash
# First, clean the SQL file for PostgreSQL compatibility
# (Use the find/replace steps from Method 1)

# Then import
psql -U postgres -d willowsrta -f export_postgres.sql
```

**Using pgAdmin:**

1. Open pgAdmin
2. Right-click on `willowsrta` database
3. Select "Query Tool"
4. File → Open → Select `export_postgres.sql`
5. Click Execute (⚡ icon)

### Option 2: Import CSV Files

**Using psql:**

```bash
# Connect to database
psql -U postgres -d willowsrta

# Import members
\COPY members FROM 'members.csv' CSV HEADER;

# Import users
\COPY users FROM 'users.csv' CSV HEADER;

# Import otp_codes
\COPY otp_codes FROM 'otp_codes.csv' CSV HEADER;

# Reset auto-increment sequences
SELECT setval('members_id_seq', (SELECT MAX(id) FROM members));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('otp_codes_id_seq', (SELECT MAX(id) FROM otp_codes));
```

**Using pgAdmin:**

1. Right-click table (e.g., `members`)
2. Select "Import/Export Data"
3. Toggle to "Import"
4. Select CSV file
5. Options:
   - Header: Yes
   - Delimiter: , (comma)
6. Click OK

---

## 🛠️ Complete Export Script

I'll create a helper script for you:

**File: `export-h2-data.sql`**

```sql
-- H2 Database Export Script
-- Run this in H2 Console while app is running

-- Export all tables to CSV
CALL CSVWRITE('export/members.csv', 'SELECT * FROM members', 'charset=UTF-8');
CALL CSVWRITE('export/users.csv', 'SELECT * FROM users', 'charset=UTF-8');
CALL CSVWRITE('export/otp_codes.csv', 'SELECT * FROM otp_codes', 'charset=UTF-8');

-- Also create SQL backup
SCRIPT TO 'export/full_backup.sql';

-- Show what was exported
SELECT 'Members: ' || COUNT(*) AS exported FROM members;
SELECT 'Users: ' || COUNT(*) AS exported FROM users;
SELECT 'OTP Codes: ' || COUNT(*) AS exported FROM otp_codes;
```

**To use:**
1. Create `export/` folder in project root
2. Copy script to H2 Console
3. Execute
4. Check `export/` folder for files

---

## 📋 Complete Migration Workflow

### Step-by-Step Process:

**Phase 1: Backup H2 Data**

1. ✅ Start app with H2 configuration
2. ✅ Access H2 Console (http://localhost:8082/h2-console)
3. ✅ Run export script (SQL or CSV)
4. ✅ Verify files created
5. ✅ **Keep H2 running** - don't change config yet!

**Phase 2: Setup PostgreSQL**

6. ✅ Install PostgreSQL
7. ✅ Create `willowsrta` database
8. ✅ Keep database empty for now

**Phase 3: Update Application**

9. ✅ Update `pom.xml` (add PostgreSQL, comment out H2)
10. ✅ Update `application.properties` (PostgreSQL config)
11. ✅ **Don't start app yet!**

**Phase 4: Import Data**

12. ✅ Start app once (creates empty tables in PostgreSQL)
13. ✅ Stop app
14. ✅ Import data (SQL file or CSV)
15. ✅ Reset sequences
16. ✅ Verify data imported

**Phase 5: Test**

17. ✅ Start app
18. ✅ Login with existing admin
19. ✅ Check members list
20. ✅ Test all features
21. ✅ Success! 🎉

---

## 🔍 Verification Checklist

### After Import, Verify:

**In PostgreSQL (using pgAdmin or psql):**

```sql
-- Count records
SELECT 'Members: ' || COUNT(*) FROM members;
SELECT 'Users: ' || COUNT(*) FROM users;

-- Check members data
SELECT id, full_name, email, membership_status FROM members LIMIT 5;

-- Check users data
SELECT id, username, role, enabled FROM users;

-- Verify relationships
SELECT m.full_name, u.username, u.role
FROM members m
LEFT JOIN users u ON u.member_id = m.id
WHERE m.has_user_account = true;

-- Check admin account
SELECT * FROM users WHERE username = 'admin';
```

**Expected results:**
- Same number of records as H2
- Admin account exists
- Member-User relationships intact
- All data fields populated

---

## 🐛 Troubleshooting

### "Export.sql not found"

**Problem:** File created in wrong location

**Solution:**
```bash
# Find where it was created
# Windows
dir /s export.sql

# Mac/Linux  
find . -name export.sql
```

### "Cannot import - table already exists"

**Problem:** Tables created by Hibernate

**Solution:**
```sql
-- Drop all tables first
DROP TABLE IF EXISTS otp_codes CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS members CASCADE;

-- Then import
```

### "Sequence out of sync"

**Problem:** Auto-increment not starting at right number

**Solution:**
```sql
-- Reset all sequences after import
SELECT setval('members_id_seq', (SELECT MAX(id) FROM members));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('otp_codes_id_seq', (SELECT MAX(id) FROM otp_codes));
```

### "Foreign key constraint violation"

**Problem:** Importing in wrong order

**Solution:**
```sql
-- Import in this order:
-- 1. members (no dependencies)
-- 2. users (depends on members)
-- 3. otp_codes (depends on users)
```

### "Character encoding issues"

**Problem:** Special characters corrupted

**Solution:**
```sql
-- Export with UTF-8
CALL CSVWRITE('members.csv', 'SELECT * FROM members', 'charset=UTF-8');

-- Import with UTF-8
\COPY members FROM 'members.csv' CSV HEADER ENCODING 'UTF8';
```

---

## 💡 Pro Tips

### 1. **Always Keep a Backup**

```bash
# Backup the entire data folder
cp -r data/ data_backup_2026-02-09/

# Or zip it
zip -r h2_backup_2026-02-09.zip data/
```

### 2. **Test Import in Separate Database First**

```sql
-- Create test database
CREATE DATABASE willowsrta_test;

-- Import there first
psql -U postgres -d willowsrta_test -f export.sql

-- Verify it works, then import to real database
```

### 3. **Export OTP Codes Last (or skip)**

OTP codes expire in 10 minutes, so you probably don't need to migrate them. Let PostgreSQL start fresh.

### 4. **Document What You Exported**

Create a file: `export_log.txt`
```
Export Date: 2026-02-09 15:30
Members: 45 records
Users: 38 records
Admin account: admin (DO NOT DELETE)
Source: H2 file database
Destination: PostgreSQL
```

---

## 📝 Quick Command Reference

### Export Commands (H2):

```sql
-- Full SQL backup
SCRIPT TO 'backup.sql';

-- Individual tables to CSV
CALL CSVWRITE('members.csv', 'SELECT * FROM members');
CALL CSVWRITE('users.csv', 'SELECT * FROM users');

-- Count records
SELECT COUNT(*) FROM members;
```

### Import Commands (PostgreSQL):

```bash
# SQL file
psql -U postgres -d willowsrta -f backup.sql

# CSV files
\COPY members FROM 'members.csv' CSV HEADER;

# Reset sequences
SELECT setval('members_id_seq', (SELECT MAX(id) FROM members));
```

---

## 🎯 Recommended Approach for You

Based on your situation, I recommend:

### **Method 1: SQL File Export**

**Why:**
- Simplest
- Preserves structure
- One file to import
- Relationships maintained

**Steps:**
1. Start app with H2
2. Open H2 Console
3. Run: `SCRIPT TO 'export.sql';`
4. Edit file for PostgreSQL compatibility
5. Setup PostgreSQL
6. Start app once (creates tables)
7. Stop app
8. Import: `psql -U postgres -d willowsrta -f export.sql`
9. Start app
10. Test!

---

## 🆘 Need Help?

If you encounter issues:

1. Check H2 Console is accessible
2. Verify files are created
3. Check file sizes (should not be 0 bytes)
4. Look for error messages in import
5. Check PostgreSQL logs
6. Verify table structures match

---

**You're ready to export your H2 data safely!** 💾✨

After export, you can proceed with PostgreSQL migration knowing your data is safe.
