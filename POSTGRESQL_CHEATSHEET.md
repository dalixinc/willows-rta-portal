# PostgreSQL Management Cheat Sheet

Quick reference for common PostgreSQL tasks.

---

## 🚀 Starting/Stopping PostgreSQL

### Windows

**Check if running:**
- Open Services (services.msc)
- Look for "postgresql-x64-14" (or your version)
- Status should be "Running"

**Start:**
- Services → postgresql → Right-click → Start

**Stop:**
- Services → postgresql → Right-click → Stop

### Mac (Homebrew)

```bash
# Start
brew services start postgresql@14

# Stop
brew services stop postgresql@14

# Restart
brew services restart postgresql@14

# Check status
brew services list | grep postgresql
```

### Linux

```bash
# Start
sudo systemctl start postgresql

# Stop
sudo systemctl stop postgresql

# Restart
sudo systemctl restart postgresql

# Check status
sudo systemctl status postgresql

# Enable on boot
sudo systemctl enable postgresql
```

---

## 💻 psql Command Line Basics

### Connecting

```bash
# Connect to postgres database
psql -U postgres

# Connect to specific database
psql -U postgres -d willowsrta

# Connect with hostname (if not localhost)
psql -U postgres -h localhost -d willowsrta
```

### psql Meta Commands

**Inside psql:**

```sql
-- List all databases
\l

-- Connect to database
\c willowsrta

-- List tables
\dt

-- Describe table
\d members
\d users

-- List all schemas
\dn

-- List all users/roles
\du

-- Show current database
SELECT current_database();

-- Show current user
SELECT current_user;

-- Quit
\q

-- Help
\?

-- SQL command help
\h SELECT
```

---

## 🗄️ Database Management

### Create Database

```sql
-- In psql:
CREATE DATABASE willowsrta;

-- With owner:
CREATE DATABASE willowsrta OWNER postgres;

-- From command line:
createdb -U postgres willowsrta
```

### Drop Database

```sql
-- In psql (disconnect from it first!):
\c postgres
DROP DATABASE willowsrta;

-- Force drop (disconnect all users):
DROP DATABASE willowsrta WITH (FORCE);

-- From command line:
dropdb -U postgres willowsrta
```

### List Databases

```sql
-- In psql:
\l

-- Or SQL:
SELECT datname FROM pg_database;

-- With sizes:
SELECT 
    datname AS database_name,
    pg_size_pretty(pg_database_size(datname)) AS size
FROM pg_database
ORDER BY pg_database_size(datname) DESC;
```

### Rename Database

```sql
-- Must disconnect from it first
\c postgres
ALTER DATABASE willowsrta RENAME TO willowsrta_backup;
```

---

## 📊 Table Management

### List Tables

```sql
-- In psql:
\dt

-- With schema:
\dt public.*

-- SQL way:
SELECT tablename 
FROM pg_catalog.pg_tables 
WHERE schemaname = 'public';
```

### Describe Table

```sql
-- In psql:
\d members

-- SQL way:
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'members'
ORDER BY ordinal_position;
```

### Table Size

```sql
-- Single table:
SELECT pg_size_pretty(pg_total_relation_size('members'));

-- All tables:
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_catalog.pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Drop Table

```sql
-- Single table:
DROP TABLE members;

-- With cascade (removes dependencies):
DROP TABLE members CASCADE;

-- If exists (won't error if missing):
DROP TABLE IF EXISTS members CASCADE;
```

---

## 🔑 User/Role Management

### Create User

```sql
-- Create user:
CREATE USER myuser WITH PASSWORD 'mypassword';

-- Create user with all privileges:
CREATE USER myuser WITH PASSWORD 'mypassword' CREATEDB CREATEROLE;

-- Create role (similar to user):
CREATE ROLE myrole WITH LOGIN PASSWORD 'mypassword';
```

### Grant Permissions

```sql
-- Grant all on database:
GRANT ALL PRIVILEGES ON DATABASE willowsrta TO myuser;

-- Grant all on all tables:
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO myuser;

-- Grant select only:
GRANT SELECT ON members TO myuser;

-- Grant specific operations:
GRANT SELECT, INSERT, UPDATE ON members TO myuser;
```

### Change Password

```sql
-- Change password:
ALTER USER postgres WITH PASSWORD 'newpassword';

-- Or:
\password postgres
```

### List Users

```sql
-- In psql:
\du

-- SQL way:
SELECT usename, usesuper, usecreatedb
FROM pg_user;
```

---

## 📤 Backup & Restore

### Backup (pg_dump)

```bash
# Backup entire database to SQL file:
pg_dump -U postgres willowsrta > backup.sql

# Backup with custom format (compressed):
pg_dump -U postgres -Fc willowsrta > backup.dump

# Backup specific tables only:
pg_dump -U postgres -t members -t users willowsrta > tables_backup.sql

# With timestamp:
pg_dump -U postgres willowsrta > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restore (psql / pg_restore)

```bash
# Restore from SQL file:
psql -U postgres -d willowsrta < backup.sql

# Restore from custom format:
pg_restore -U postgres -d willowsrta backup.dump

# Create database and restore:
createdb -U postgres willowsrta
psql -U postgres -d willowsrta < backup.sql
```

---

## 🔍 Querying Data

### Basic Queries

```sql
-- Count records:
SELECT COUNT(*) FROM members;

-- View all data:
SELECT * FROM members;

-- Limit results:
SELECT * FROM members LIMIT 10;

-- Order results:
SELECT * FROM members ORDER BY registration_date DESC;

-- Filter:
SELECT * FROM members WHERE membership_status = 'ACTIVE';

-- Multiple conditions:
SELECT * FROM members 
WHERE membership_status = 'ACTIVE' 
  AND has_user_account = TRUE;
```

### Useful Queries

```sql
-- Find duplicates:
SELECT email, COUNT(*) 
FROM members 
GROUP BY email 
HAVING COUNT(*) > 1;

-- Find orphaned records:
SELECT m.* 
FROM members m
LEFT JOIN users u ON u.member_id = m.id
WHERE m.has_user_account = TRUE AND u.id IS NULL;

-- Count by group:
SELECT membership_status, COUNT(*)
FROM members
GROUP BY membership_status;

-- Today's registrations:
SELECT * FROM members 
WHERE DATE(registration_date) = CURRENT_DATE;
```

---

## 🧹 Maintenance

### Vacuum (Clean Up)

```sql
-- Vacuum single table:
VACUUM members;

-- Vacuum with analysis:
VACUUM ANALYZE members;

-- Vacuum entire database:
VACUUM;

-- Full vacuum (reclaim space):
VACUUM FULL;
```

### Analyze (Update Statistics)

```sql
-- Analyze table:
ANALYZE members;

-- Analyze all tables:
ANALYZE;
```

### Reindex

```sql
-- Reindex table:
REINDEX TABLE members;

-- Reindex database:
REINDEX DATABASE willowsrta;
```

---

## 📊 Monitoring

### Active Connections

```sql
-- Current connections:
SELECT * FROM pg_stat_activity WHERE datname = 'willowsrta';

-- Count connections:
SELECT COUNT(*) FROM pg_stat_activity WHERE datname = 'willowsrta';

-- Kill connection:
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE pid = 12345;

-- Kill all connections to database:
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = 'willowsrta' AND pid <> pg_backend_pid();
```

### Database Statistics

```sql
-- Table statistics:
SELECT * FROM pg_stat_user_tables WHERE schemaname = 'public';

-- Index usage:
SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';

-- Database size:
SELECT pg_size_pretty(pg_database_size('willowsrta'));

-- Largest tables:
SELECT 
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 🔧 Configuration

### View Settings

```sql
-- Show all settings:
SHOW ALL;

-- Show specific setting:
SHOW max_connections;
SHOW shared_buffers;

-- SQL way:
SELECT name, setting, unit, context 
FROM pg_settings 
WHERE name LIKE '%max_connections%';
```

### Connection Info

```sql
-- Current database:
SELECT current_database();

-- Current user:
SELECT current_user;

-- PostgreSQL version:
SELECT version();

-- Server uptime:
SELECT now() - pg_postmaster_start_time() AS uptime;
```

---

## 🚨 Common Issues & Solutions

### Can't connect

```bash
# Check if PostgreSQL is running
# Mac:
brew services list | grep postgresql

# Linux:
sudo systemctl status postgresql

# Windows: Check Services
```

### "database does not exist"

```sql
-- Create it:
CREATE DATABASE willowsrta;
```

### "password authentication failed"

```sql
-- Reset password:
ALTER USER postgres WITH PASSWORD 'newpassword';

-- Or use trust authentication temporarily (pg_hba.conf)
```

### "too many connections"

```sql
-- Check current:
SELECT COUNT(*) FROM pg_stat_activity;

-- Kill idle connections:
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE state = 'idle' 
  AND state_change < now() - interval '1 hour';
```

### Database won't drop

```sql
-- Force drop (kills all connections):
DROP DATABASE willowsrta WITH (FORCE);

-- Or manually kill connections first:
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = 'willowsrta' 
  AND pid <> pg_backend_pid();
  
DROP DATABASE willowsrta;
```

---

## 📚 Quick Reference

| Task | Command |
|------|---------|
| Connect | `psql -U postgres -d willowsrta` |
| List DBs | `\l` |
| List tables | `\dt` |
| Describe table | `\d members` |
| Run SQL file | `psql -U postgres -d willowsrta -f file.sql` |
| Backup | `pg_dump -U postgres willowsrta > backup.sql` |
| Restore | `psql -U postgres -d willowsrta < backup.sql` |
| Quit | `\q` |
| Help | `\?` |

---

**Keep this handy!** 📋✨
