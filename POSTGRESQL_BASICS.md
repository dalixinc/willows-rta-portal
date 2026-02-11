# PostgreSQL Crash Course for SQL Users

Quick guide to PostgreSQL concepts and how they differ from other SQL databases.

---

## 🏗️ Database Organization Hierarchy

### The Full Structure:

```
PostgreSQL Server (Instance)
└── Database (e.g., "willowsrta")
    └── Schema (e.g., "public")
        └── Tables, Views, Functions, etc.
            ├── members
            ├── users
            └── otp_codes
```

Let's break this down:

---

## 📚 Schemas

### What is a Schema?

**Think of it as a namespace or folder within a database.**

```sql
-- Full table reference:
schema_name.table_name

-- Example:
public.members
public.users
```

### Default Schema: `public`

- Every PostgreSQL database has a `public` schema by default
- If you don't specify a schema, PostgreSQL uses `public`
- These are equivalent:

```sql
SELECT * FROM members;
SELECT * FROM public.members;
```

### Why Use Multiple Schemas?

**Organization and separation:**

```
willowsrta database
├── public (main application tables)
│   ├── members
│   ├── users
│   └── otp_codes
├── audit (audit/logging tables)
│   ├── login_history
│   └── changes_log
├── temp (temporary/staging tables)
│   └── import_staging
└── reporting (reporting views)
    ├── active_members_view
    └── monthly_stats_view
```

### Common Schema Commands:

```sql
-- Create schema
CREATE SCHEMA audit;

-- Create table in specific schema
CREATE TABLE audit.login_history (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    login_time TIMESTAMP
);

-- Query from specific schema
SELECT * FROM audit.login_history;

-- Set default schema for session
SET search_path TO audit, public;

-- List all schemas
SELECT schema_name FROM information_schema.schemata;

-- Drop schema
DROP SCHEMA audit CASCADE;  -- CASCADE drops everything in it
```

### MySQL Comparison:

| MySQL | PostgreSQL |
|-------|------------|
| Database = namespace | Schema = namespace |
| `USE database_name;` | `SET search_path TO schema_name;` |
| No schemas | Has schemas |

**MySQL:** Databases are separate containers  
**PostgreSQL:** One database, multiple schemas inside

---

## 📖 Catalogs (System Catalogs)

### What is a Catalog?

**PostgreSQL's internal system tables that store metadata about your database.**

Think of it as "the database about the database."

### Key Catalogs:

```sql
-- pg_catalog schema contains all system tables
pg_catalog.pg_tables      -- Info about all tables
pg_catalog.pg_indexes     -- Info about indexes
pg_catalog.pg_roles       -- User/role information
pg_catalog.pg_database    -- Database information
```

### You've Already Used Catalogs!

```sql
-- List all tables (uses catalog)
\dt

-- Behind the scenes, this runs:
SELECT * FROM pg_catalog.pg_tables 
WHERE schemaname = 'public';

-- List all databases
\l

-- Behind the scenes:
SELECT * FROM pg_catalog.pg_database;
```

### Useful Catalog Queries:

```sql
-- See all tables in your database
SELECT tablename 
FROM pg_catalog.pg_tables 
WHERE schemaname = 'public';

-- See table columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'members';

-- See all indexes
SELECT indexname, tablename 
FROM pg_catalog.pg_indexes 
WHERE schemaname = 'public';

-- See database size
SELECT pg_size_pretty(pg_database_size('willowsrta'));

-- See table size
SELECT pg_size_pretty(pg_total_relation_size('members'));

-- See all users/roles
SELECT rolname FROM pg_catalog.pg_roles;
```

### information_schema vs pg_catalog

**Both show metadata, but:**

| information_schema | pg_catalog |
|-------------------|------------|
| SQL standard (portable) | PostgreSQL-specific |
| Limited info | Complete info |
| Use for basic queries | Use for advanced queries |

**Example:**

```sql
-- Standard way (works on MySQL, PostgreSQL, etc.)
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';

-- PostgreSQL way (more powerful)
SELECT tablename 
FROM pg_catalog.pg_tables 
WHERE schemaname = 'public';
```

---

## 🔄 Casts (Type Conversion)

### What is a Cast?

**Converting data from one type to another.**

### Syntax:

```sql
-- Method 1: CAST function (SQL standard)
CAST(value AS target_type)

-- Method 2: :: operator (PostgreSQL shorthand)
value::target_type
```

### Common Casts:

```sql
-- String to Integer
SELECT CAST('123' AS INTEGER);
SELECT '123'::INTEGER;

-- String to Date
SELECT CAST('2026-02-09' AS DATE);
SELECT '2026-02-09'::DATE;

-- Integer to String
SELECT CAST(123 AS VARCHAR);
SELECT 123::VARCHAR;

-- Timestamp to Date
SELECT CAST(NOW() AS DATE);
SELECT NOW()::DATE;

-- Boolean to String
SELECT TRUE::TEXT;  -- 'true'

-- JSON to Text
SELECT '{"name":"John"}'::JSON;
```

### In Your Application:

```sql
-- Get registration date as formatted string
SELECT full_name, 
       registration_date::DATE AS reg_date
FROM members;

-- Count members registered per month
SELECT DATE_TRUNC('month', registration_date)::DATE AS month,
       COUNT(*) AS new_members
FROM members
GROUP BY month;

-- Convert flat_number to uppercase
SELECT UPPER(flat_number::TEXT) AS flat
FROM members;
```

### Implicit vs Explicit Casts:

```sql
-- Implicit (automatic)
SELECT 1 + 1.5;  -- Result: 2.5 (integer→numeric)

-- Explicit (you specify)
SELECT '5'::INTEGER + 3;  -- Must cast string to integer

-- This fails:
SELECT '5' + 3;  -- ERROR: Can't add string and integer

-- This works:
SELECT '5'::INTEGER + 3;  -- Result: 8
```

### Common Cast Scenarios:

```sql
-- 1. User input is always text, need to compare as number
SELECT * FROM members 
WHERE flat_number::INTEGER > 10;

-- 2. Date formatting
SELECT full_name,
       registration_date::DATE AS date,
       TO_CHAR(registration_date, 'DD Mon YYYY') AS formatted_date
FROM members;

-- 3. JSON data extraction
SELECT member_data->>'name' AS name,
       (member_data->>'age')::INTEGER AS age
FROM member_json;

-- 4. Boolean from string
SELECT 'true'::BOOLEAN;   -- true
SELECT 'yes'::BOOLEAN;    -- true
SELECT '1'::BOOLEAN;      -- true
SELECT 'false'::BOOLEAN;  -- false
```

---

## 🎯 Quick Reference

### Schemas:

```sql
-- Create
CREATE SCHEMA schema_name;

-- Use (set search path)
SET search_path TO schema_name;

-- Create table in schema
CREATE TABLE schema_name.table_name (...);

-- Query
SELECT * FROM schema_name.table_name;

-- Drop
DROP SCHEMA schema_name CASCADE;
```

### Catalogs (System Info):

```sql
-- List tables
SELECT * FROM pg_catalog.pg_tables WHERE schemaname = 'public';

-- List columns
SELECT * FROM information_schema.columns WHERE table_name = 'members';

-- Database size
SELECT pg_size_pretty(pg_database_size('willowsrta'));

-- Table size
SELECT pg_size_pretty(pg_total_relation_size('members'));
```

### Casts:

```sql
-- String → Integer
'123'::INTEGER

-- String → Date
'2026-02-09'::DATE

-- Number → String
123::TEXT

-- Timestamp → Date
NOW()::DATE

-- Using CAST function
CAST('123' AS INTEGER)
```

---

## 🔍 Practical Examples for Your App

### Example 1: Find Members by Flat Number Range

```sql
-- Convert flat_number to integer and filter
SELECT full_name, flat_number
FROM members
WHERE flat_number::INTEGER BETWEEN 10 AND 20
ORDER BY flat_number::INTEGER;
```

### Example 2: Member Registration Report by Month

```sql
-- Cast timestamp to date, group by month
SELECT DATE_TRUNC('month', registration_date)::DATE AS month,
       COUNT(*) AS new_members,
       COUNT(*) FILTER (WHERE has_user_account = true) AS with_accounts
FROM members
GROUP BY month
ORDER BY month DESC;
```

### Example 3: Find Locked Accounts Expiring Soon

```sql
-- Using catalog to check table structure, then query
SELECT u.username,
       u.account_locked_until,
       (u.account_locked_until - NOW())::INTERVAL AS time_remaining
FROM users u
WHERE u.account_locked_until > NOW()
ORDER BY u.account_locked_until;
```

### Example 4: Database Health Check

```sql
-- Using catalogs for monitoring
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    n_tup_ins AS total_inserts,
    n_tup_upd AS total_updates,
    n_tup_del AS total_deletes
FROM pg_catalog.pg_stat_user_tables
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 🆚 PostgreSQL vs Other Databases

### MySQL → PostgreSQL

| Concept | MySQL | PostgreSQL |
|---------|-------|------------|
| **Namespace** | Database | Schema |
| **Switch Context** | `USE dbname;` | `SET search_path TO schema;` |
| **Type Cast** | Limited | `::` operator |
| **System Info** | `SHOW TABLES;` | `\dt` or pg_catalog |
| **Auto Increment** | `AUTO_INCREMENT` | `SERIAL` |
| **String Concat** | `CONCAT()` | `\|\|` operator |

### SQL Server → PostgreSQL

| Concept | SQL Server | PostgreSQL |
|---------|------------|------------|
| **Schema** | Same concept | Same concept |
| **Catalog** | sys.tables | pg_catalog.pg_tables |
| **Type Cast** | `CONVERT()` | `::` or `CAST()` |
| **Top N** | `SELECT TOP 10` | `SELECT ... LIMIT 10` |

---

## 💡 PostgreSQL-Specific Features You'll Love

### 1. ARRAY Type

```sql
-- Store multiple values in one column
CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    tags TEXT[]  -- Array of strings
);

INSERT INTO events (tags) VALUES (ARRAY['meeting', 'urgent', 'committee']);

-- Query arrays
SELECT * FROM events WHERE 'urgent' = ANY(tags);
```

### 2. JSON/JSONB Support

```sql
-- Store JSON data
CREATE TABLE preferences (
    user_id INTEGER,
    settings JSONB
);

INSERT INTO preferences VALUES (1, '{"theme": "dark", "notifications": true}');

-- Query JSON
SELECT settings->>'theme' AS theme FROM preferences WHERE user_id = 1;
```

### 3. Full Text Search

```sql
-- Search in text
SELECT * FROM members 
WHERE to_tsvector('english', full_name) @@ to_tsquery('english', 'John');
```

### 4. Common Table Expressions (CTE)

```sql
-- WITH clause for complex queries
WITH active_members AS (
    SELECT * FROM members WHERE membership_status = 'ACTIVE'
),
with_accounts AS (
    SELECT * FROM active_members WHERE has_user_account = true
)
SELECT COUNT(*) FROM with_accounts;
```

---

## 🎓 Essential Commands

### psql Commands (In PostgreSQL Terminal)

```sql
\l                  -- List databases
\c dbname           -- Connect to database
\dt                 -- List tables
\d table_name       -- Describe table
\du                 -- List users/roles
\dn                 -- List schemas
\df                 -- List functions
\dv                 -- List views
\q                  -- Quit

\x                  -- Toggle expanded display (good for wide results)
\timing             -- Show query execution time
\i filename.sql     -- Execute SQL from file
```

### Connection String Format

```
postgresql://username:password@host:port/database

Example:
postgresql://postgres:mypassword@localhost:5432/willowsrta
```

---

## 🎯 For Your Willows RTA App

### Your Database Structure:

```
PostgreSQL Server (localhost:5432)
└── willowsrta (database)
    └── public (schema)
        ├── members (table)
        ├── users (table)
        └── otp_codes (table)
```

### Useful Queries:

```sql
-- See your tables
\dt

-- Describe members table
\d members

-- Count active members
SELECT COUNT(*) FROM members WHERE membership_status = 'ACTIVE';

-- See locked accounts
SELECT username, account_locked_until 
FROM users 
WHERE account_locked_until > NOW();

-- Database size
SELECT pg_size_pretty(pg_database_size('willowsrta'));

-- Find members without accounts
SELECT full_name, email 
FROM members 
WHERE has_user_account = false;

-- Member registration by month
SELECT DATE_TRUNC('month', registration_date)::DATE AS month,
       COUNT(*) 
FROM members 
GROUP BY month 
ORDER BY month;
```

---

## 📚 Resources

**Official Docs:**
- PostgreSQL Documentation: https://www.postgresql.org/docs/
- Tutorial: https://www.postgresqltutorial.com/

**Practice:**
- pgExercises: https://pgexercises.com/
- SQL Fiddle: http://sqlfiddle.com/

**Cheat Sheets:**
- psql Commands: https://www.postgresql.org/docs/current/app-psql.html
- PostgreSQL Cheat Sheet: https://www.postgresqltutorial.com/postgresql-cheat-sheet/

---

## ✅ TL;DR - Quick Answers

**Schema?**  
→ Like a folder for tables. Default is `public`. Use: `schema_name.table_name`

**Catalog?**  
→ System tables with metadata. Query with `pg_catalog.pg_*` or `information_schema.*`

**Cast?**  
→ Convert types. Use `value::TYPE` or `CAST(value AS TYPE)`

**Difference from MySQL?**  
→ PostgreSQL has schemas inside databases. MySQL has separate databases. PostgreSQL has `::` for casts.

**How to see my tables?**  
→ `\dt` in psql or query `pg_catalog.pg_tables`

---

**You're ready for PostgreSQL!** 🐘✨
