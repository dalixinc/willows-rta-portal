# PostgreSQL for MySQL/Oracle Users - Quick Learn Guide

Hands-on guide with your actual data to learn PostgreSQL and pgAdmin.

---

## 🎓 Learning by Doing - Using Your Data

### Step 1: Connect in pgAdmin

**Open pgAdmin:**
1. Start pgAdmin 4
2. Expand "Servers" in left panel
3. Click "PostgreSQL 14" (or your version)
4. Enter password
5. Expand "Databases" → "willowsrta"

**You should see:**
```
willowsrta
├── Schemas
│   └── public
│       └── Tables
│           ├── members
│           ├── users
│           └── otp_codes
```

---

## 🔍 Key Differences from MySQL/Oracle

### 1. Case Sensitivity

**MySQL:**
```sql
-- Case insensitive by default
SELECT * FROM Members;  -- Works
SELECT * FROM MEMBERS;  -- Works
```

**PostgreSQL:**
```sql
-- Case insensitive for unquoted identifiers
SELECT * FROM members;  -- ✅ Works (lowercase)
SELECT * FROM Members;  -- ✅ Works (converted to lowercase)
SELECT * FROM MEMBERS;  -- ✅ Works (converted to lowercase)

-- Case sensitive if quoted
SELECT * FROM "Members";  -- ❌ Fails (looks for exact case)

-- Best practice: Always use lowercase, no quotes
```

**Try it:**
```sql
-- All these work the same:
SELECT COUNT(*) FROM members;
SELECT COUNT(*) FROM Members;
SELECT COUNT(*) FROM MEMBERS;
```

---

### 2. String Concatenation

**MySQL:**
```sql
SELECT CONCAT(first_name, ' ', last_name) FROM users;
```

**Oracle:**
```sql
SELECT first_name || ' ' || last_name FROM users;
```

**PostgreSQL:**
```sql
-- Uses Oracle style (||)
SELECT full_name || ' (' || email || ')' AS display_name
FROM members;
```

**Try it with your data:**
```sql
-- Concatenate member info
SELECT 
    full_name || ' - Flat ' || flat_number AS member_info,
    'Email: ' || email AS contact
FROM members
ORDER BY id;
```

**Expected output:**
```
      member_info           |            contact
----------------------------+--------------------------------
 Goig - Flat 7 Windings... | Email: dork@gmail.com
 Dale Macdonald - Flat 9... | Email: dale_macdonald@hotmail.com
 bob - Flat 4 Clustoid     | Email: bob@a.com
 Goig - Flat 7 Windings... | Email: dork2@gmail.com
```

---

### 3. Limit Rows

**MySQL:**
```sql
SELECT * FROM users LIMIT 10;
```

**Oracle:**
```sql
SELECT * FROM users WHERE ROWNUM <= 10;
```

**PostgreSQL:**
```sql
-- Same as MySQL
SELECT * FROM members LIMIT 3;

-- With offset (skip first 2, show next 3)
SELECT * FROM members LIMIT 3 OFFSET 2;

-- Or using FETCH (SQL standard)
SELECT * FROM members FETCH FIRST 3 ROWS ONLY;
```

**Try it:**
```sql
-- First 2 members
SELECT full_name, email FROM members LIMIT 2;

-- Skip first member, show next 2
SELECT full_name, email FROM members LIMIT 2 OFFSET 1;
```

---

### 4. Auto Increment

**MySQL:**
```sql
CREATE TABLE test (
    id INT AUTO_INCREMENT PRIMARY KEY
);
```

**Oracle:**
```sql
CREATE SEQUENCE test_seq;
CREATE TABLE test (
    id NUMBER DEFAULT test_seq.NEXTVAL
);
```

**PostgreSQL:**
```sql
-- Option 1: SERIAL (shorthand)
CREATE TABLE test (
    id SERIAL PRIMARY KEY
);

-- Option 2: BIGSERIAL (for larger numbers)
CREATE TABLE test (
    id BIGSERIAL PRIMARY KEY
);

-- Behind the scenes, this creates:
-- - A sequence: test_id_seq
-- - Default value: nextval('test_id_seq')
```

**See your sequences:**
```sql
-- List all sequences
SELECT sequence_name FROM information_schema.sequences;

-- Check current value
SELECT currval('members_id_seq');  -- Current value
SELECT nextval('members_id_seq');  -- Get next value
SELECT setval('members_id_seq', 10);  -- Set to 10
```

**Try it:**
```sql
-- See your sequences
\ds

-- Or in SQL:
SELECT schemaname, sequencename 
FROM pg_catalog.pg_sequences 
WHERE schemaname = 'public';
```

---

### 5. Boolean Type

**MySQL:**
```sql
-- Uses TINYINT(1)
-- 0 = false, 1 = true
```

**Oracle:**
```sql
-- No native boolean
-- Uses NUMBER(1) or VARCHAR2
```

**PostgreSQL:**
```sql
-- Native BOOLEAN type
-- Values: TRUE, FALSE, NULL

SELECT * FROM members WHERE has_user_account = TRUE;
SELECT * FROM members WHERE has_user_account = FALSE;

-- Can also use:
-- 't', 'true', 'y', 'yes', 'on', '1'  → TRUE
-- 'f', 'false', 'n', 'no', 'off', '0' → FALSE
```

**Try it:**
```sql
-- Members with login accounts
SELECT full_name, has_user_account 
FROM members 
WHERE has_user_account = TRUE;

-- Members without login accounts
SELECT full_name, has_user_account 
FROM members 
WHERE has_user_account = FALSE;

-- Count each
SELECT 
    has_user_account,
    COUNT(*) as count
FROM members
GROUP BY has_user_account;
```

---

### 6. Date/Time Functions

**MySQL:**
```sql
SELECT NOW();
SELECT CURDATE();
SELECT DATE_FORMAT(date_col, '%Y-%m-%d');
```

**Oracle:**
```sql
SELECT SYSDATE FROM DUAL;
SELECT TO_CHAR(date_col, 'YYYY-MM-DD') FROM table;
```

**PostgreSQL:**
```sql
-- Current timestamp
SELECT NOW();                    -- 2026-02-11 16:30:45.123
SELECT CURRENT_TIMESTAMP;        -- Same
SELECT CURRENT_DATE;             -- 2026-02-11
SELECT CURRENT_TIME;             -- 16:30:45.123

-- Format dates
SELECT TO_CHAR(registration_date, 'DD Mon YYYY') FROM members;
SELECT TO_CHAR(registration_date, 'YYYY-MM-DD HH24:MI:SS') FROM members;

-- Extract parts
SELECT EXTRACT(YEAR FROM registration_date) FROM members;
SELECT EXTRACT(MONTH FROM registration_date) FROM members;

-- Date arithmetic
SELECT registration_date + INTERVAL '1 day' FROM members;
SELECT registration_date - INTERVAL '1 month' FROM members;
```

**Try it:**
```sql
-- Format your registration dates
SELECT 
    full_name,
    registration_date AS raw,
    TO_CHAR(registration_date, 'DD Mon YYYY HH24:MI') AS formatted,
    EXTRACT(YEAR FROM registration_date) AS year,
    AGE(NOW(), registration_date) AS how_long_ago
FROM members
ORDER BY registration_date;
```

---

### 7. ISNULL / IFNULL / NVL

**MySQL:**
```sql
SELECT IFNULL(column, 'default') FROM table;
```

**Oracle:**
```sql
SELECT NVL(column, 'default') FROM table;
```

**PostgreSQL:**
```sql
-- Option 1: COALESCE (SQL standard, preferred)
SELECT COALESCE(account_creation_method, 'SELF_REGISTERED') 
FROM members;

-- Option 2: NULLIF
SELECT NULLIF(value, 0);  -- Returns NULL if value = 0

-- Your data has some NULLs in account_creation_method
SELECT 
    full_name,
    COALESCE(account_creation_method, 'SELF_REGISTERED') AS how_created
FROM members;
```

**Try it:**
```sql
-- Show creation method, defaulting to SELF_REGISTERED
SELECT 
    full_name,
    account_creation_method,
    COALESCE(account_creation_method, 'SELF_REGISTERED') AS creation_type
FROM members
ORDER BY id;
```

---

### 8. String Functions

**MySQL:**
```sql
SELECT CONCAT(str1, str2);
SELECT SUBSTRING(str, 1, 5);
SELECT UPPER(str);
SELECT LOWER(str);
```

**PostgreSQL:**
```sql
-- Concatenation
SELECT 'Hello' || ' ' || 'World';  -- Hello World
SELECT CONCAT('Hello', ' ', 'World');  -- Also works

-- Substring
SELECT SUBSTRING('Hello World' FROM 1 FOR 5);  -- Hello
SELECT SUBSTRING('Hello World', 1, 5);  -- Also works
SELECT LEFT('Hello World', 5);  -- Hello
SELECT RIGHT('Hello World', 5);  -- World

-- Case
SELECT UPPER('hello');  -- HELLO
SELECT LOWER('HELLO');  -- hello
SELECT INITCAP('hello world');  -- Hello World

-- Trim
SELECT TRIM('  hello  ');  -- 'hello'
SELECT LTRIM('  hello');  -- 'hello'
SELECT RTRIM('hello  ');  -- 'hello'

-- Replace
SELECT REPLACE('hello world', 'world', 'PostgreSQL');
```

**Try it:**
```sql
-- Play with your member names
SELECT 
    full_name,
    UPPER(full_name) AS uppercase,
    LOWER(full_name) AS lowercase,
    INITCAP(LOWER(full_name)) AS proper_case,
    LENGTH(full_name) AS name_length,
    LEFT(full_name, 3) AS first_3_chars
FROM members;
```

---

## 🎯 PostgreSQL-Specific Cool Features

### 1. RETURNING Clause

**MySQL/Oracle:**
```sql
-- Need two queries
INSERT INTO members (...) VALUES (...);
SELECT LAST_INSERT_ID();  -- MySQL
SELECT member_seq.CURRVAL FROM DUAL;  -- Oracle
```

**PostgreSQL:**
```sql
-- Get inserted row back in one query
INSERT INTO members (full_name, email, ...) 
VALUES ('New Member', 'new@example.com', ...)
RETURNING id, full_name, email;

-- Works with UPDATE and DELETE too
UPDATE members SET membership_status = 'SUSPENDED' 
WHERE id = 1
RETURNING *;

DELETE FROM members WHERE id = 5
RETURNING full_name, email;
```

---

### 2. Array Type

**PostgreSQL has native arrays:**

```sql
-- Create table with array
CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    tags TEXT[]  -- Array of strings
);

-- Insert
INSERT INTO events (tags) VALUES (ARRAY['meeting', 'urgent', 'committee']);
INSERT INTO events (tags) VALUES ('{"planning", "2026", "budget"}');

-- Query
SELECT * FROM events WHERE 'urgent' = ANY(tags);
SELECT * FROM events WHERE tags @> ARRAY['meeting'];  -- Contains

-- Array functions
SELECT array_length(tags, 1) FROM events;
SELECT unnest(tags) FROM events;  -- Expand to rows
```

---

### 3. JSON/JSONB Support

**PostgreSQL has excellent JSON support:**

```sql
-- Create table with JSON
CREATE TABLE settings (
    user_id INTEGER,
    preferences JSONB  -- Binary JSON (faster)
);

-- Insert
INSERT INTO settings VALUES (1, '{"theme": "dark", "notifications": true}');

-- Query JSON
SELECT preferences->>'theme' FROM settings;  -- Get as text
SELECT preferences->'notifications' FROM settings;  -- Get as JSON
SELECT * FROM settings WHERE preferences->>'theme' = 'dark';

-- Update JSON
UPDATE settings 
SET preferences = jsonb_set(preferences, '{theme}', '"light"')
WHERE user_id = 1;
```

---

### 4. WITH (Common Table Expressions)

**Like Oracle, better than MySQL:**

```sql
-- Build complex queries step by step
WITH active_members AS (
    SELECT * FROM members WHERE membership_status = 'ACTIVE'
),
members_with_accounts AS (
    SELECT * FROM active_members WHERE has_user_account = TRUE
)
SELECT 
    COUNT(*) as total_active_with_accounts,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM active_members), 2) as percentage
FROM members_with_accounts;
```

**Try it with your data:**
```sql
-- Multi-step analysis
WITH member_stats AS (
    SELECT 
        membership_status,
        has_user_account,
        COUNT(*) as count
    FROM members
    GROUP BY membership_status, has_user_account
)
SELECT 
    membership_status,
    SUM(count) as total,
    SUM(CASE WHEN has_user_account THEN count ELSE 0 END) as with_accounts,
    SUM(CASE WHEN NOT has_user_account THEN count ELSE 0 END) as without_accounts
FROM member_stats
GROUP BY membership_status;
```

---

## 🔧 pgAdmin Tips

### Running Queries

1. **Open Query Tool:**
   - Right-click database → "Query Tool"
   - Or: Tools menu → Query Tool

2. **Write Query:**
   - Type in top panel
   - Syntax highlighting included!

3. **Execute:**
   - Click ⚡ (Execute/Refresh) button
   - Or press F5
   - Or highlight part of query and F5 (runs selection only)

4. **View Results:**
   - Bottom panel shows results
   - Can export to CSV, copy, etc.

---

### Useful pgAdmin Features

**1. View Table Data:**
```
Right-click table → View/Edit Data → All Rows
```

**2. Table Properties:**
```
Right-click table → Properties
See columns, constraints, indexes, etc.
```

**3. ER Diagram:**
```
Right-click database → ERD For Database
Visual representation of relationships
```

**4. Explain Plan:**
```
Write query → Click EXPLAIN button
See query execution plan (like Oracle's EXPLAIN PLAN)
```

**5. Auto-complete:**
```
Start typing table/column name
Press Ctrl+Space for suggestions
```

**6. Query History:**
```
View menu → Query History
See all queries you've run
```

---

## 📚 Practice Queries Using Your Data

### Easy Level

```sql
-- 1. Count members
SELECT COUNT(*) FROM members;

-- 2. List member names
SELECT full_name FROM members ORDER BY full_name;

-- 3. Members with email addresses
SELECT full_name, email FROM members WHERE email LIKE '%@%';

-- 4. Count by status
SELECT membership_status, COUNT(*) 
FROM members 
GROUP BY membership_status;
```

---

### Medium Level

```sql
-- 1. Members with and without accounts
SELECT 
    CASE WHEN has_user_account THEN 'Has Account' ELSE 'No Account' END AS status,
    COUNT(*) as count
FROM members
GROUP BY has_user_account;

-- 2. Join members and users
SELECT 
    m.full_name,
    m.email,
    u.role,
    u.system_admin
FROM members m
LEFT JOIN users u ON u.member_id = m.id;

-- 3. Users who are admins
SELECT 
    m.full_name,
    u.username,
    u.role
FROM users u
LEFT JOIN members m ON m.id = u.member_id
WHERE u.role = 'ROLE_ADMIN';

-- 4. Registration summary
SELECT 
    DATE(registration_date) as reg_date,
    COUNT(*) as registrations
FROM members
GROUP BY DATE(registration_date)
ORDER BY reg_date;
```

---

### Advanced Level

```sql
-- 1. Member account analysis
WITH account_summary AS (
    SELECT 
        COUNT(*) FILTER (WHERE has_user_account) as with_accounts,
        COUNT(*) FILTER (WHERE NOT has_user_account) as without_accounts,
        COUNT(*) as total
    FROM members
)
SELECT 
    *,
    ROUND(with_accounts * 100.0 / total, 2) as pct_with_accounts
FROM account_summary;

-- 2. User role distribution
SELECT 
    CASE 
        WHEN role = 'ROLE_ADMIN' AND system_admin THEN 'System Admin'
        WHEN role = 'ROLE_ADMIN' THEN 'Admin'
        WHEN role = 'ROLE_MEMBER' THEN 'Member'
        ELSE 'Unknown'
    END as user_type,
    COUNT(*) as count,
    STRING_AGG(username, ', ') as usernames
FROM users
GROUP BY 
    CASE 
        WHEN role = 'ROLE_ADMIN' AND system_admin THEN 'System Admin'
        WHEN role = 'ROLE_ADMIN' THEN 'Admin'
        WHEN role = 'ROLE_MEMBER' THEN 'Member'
        ELSE 'Unknown'
    END;

-- 3. Find orphaned records
-- Members with has_user_account=TRUE but no user record
SELECT m.*
FROM members m
LEFT JOIN users u ON u.member_id = m.id
WHERE m.has_user_account = TRUE AND u.id IS NULL;

-- 4. Window functions (row numbers)
SELECT 
    full_name,
    registration_date,
    ROW_NUMBER() OVER (ORDER BY registration_date) as registration_order,
    RANK() OVER (ORDER BY registration_date) as registration_rank
FROM members;
```

---

## 🎓 Exercises to Practice

### Exercise 1: Data Exploration
```sql
-- Count how many members registered on each date
-- Group by the date part only (not time)
-- Order by date

-- Your solution here:
```

<details>
<summary>Solution</summary>

```sql
SELECT 
    DATE(registration_date) as reg_date,
    COUNT(*) as members_registered
FROM members
GROUP BY DATE(registration_date)
ORDER BY reg_date;
```
</details>

---

### Exercise 2: String Manipulation
```sql
-- Create an email-friendly display name
-- Format: "NAME (Flat X)"
-- Example: "Dale Macdonald (Flat 9 Windings house)"

-- Your solution here:
```

<details>
<summary>Solution</summary>

```sql
SELECT 
    full_name || ' (Flat ' || flat_number || ')' AS display_name,
    email
FROM members
ORDER BY full_name;
```
</details>

---

### Exercise 3: Aggregation
```sql
-- Show count and percentage of members:
-- - With accounts
-- - Without accounts
-- Use FILTER clause

-- Your solution here:
```

<details>
<summary>Solution</summary>

```sql
SELECT 
    COUNT(*) as total_members,
    COUNT(*) FILTER (WHERE has_user_account = TRUE) as with_accounts,
    COUNT(*) FILTER (WHERE has_user_account = FALSE) as without_accounts,
    ROUND(COUNT(*) FILTER (WHERE has_user_account = TRUE) * 100.0 / COUNT(*), 2) as pct_with_accounts
FROM members;
```
</details>

---

## 🔍 PostgreSQL System Queries

**These are super useful for learning:**

```sql
-- See all tables in current database
SELECT tablename 
FROM pg_catalog.pg_tables 
WHERE schemaname = 'public';

-- See table structure
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'members'
ORDER BY ordinal_position;

-- See indexes
SELECT indexname, indexdef
FROM pg_catalog.pg_indexes
WHERE schemaname = 'public'
AND tablename = 'members';

-- See foreign keys
SELECT
    tc.table_name, 
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
AND tc.table_name = 'users';

-- Database size
SELECT pg_size_pretty(pg_database_size('willowsrta'));

-- Table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_catalog.pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 📖 Quick Reference

### MySQL → PostgreSQL

| Feature | MySQL | PostgreSQL |
|---------|-------|------------|
| Concat | `CONCAT(a,b)` | `a \|\| b` |
| Limit | `LIMIT 10` | `LIMIT 10` (same) |
| Offset | `LIMIT 10,5` | `LIMIT 5 OFFSET 10` |
| Auto-increment | `AUTO_INCREMENT` | `SERIAL` |
| If NULL | `IFNULL(col,'default')` | `COALESCE(col,'default')` |
| Date format | `DATE_FORMAT()` | `TO_CHAR()` |
| Top N | `LIMIT N` | `LIMIT N` or `FETCH FIRST` |
| String length | `LENGTH()` | `LENGTH()` (same) |

### Oracle → PostgreSQL

| Feature | Oracle | PostgreSQL |
|---------|--------|------------|
| Concat | `\|\|` | `\|\|` (same) |
| Limit | `ROWNUM` | `LIMIT` |
| Sequence | `seq.NEXTVAL` | `nextval('seq')` |
| If NULL | `NVL()` | `COALESCE()` |
| Date format | `TO_CHAR()` | `TO_CHAR()` (same) |
| Sysdate | `SYSDATE` | `NOW()` |
| Dual table | `FROM DUAL` | Not needed |

---

Happy learning! 🎓🐘

Run these queries, experiment in pgAdmin, and you'll be a PostgreSQL pro in no time!
