# Database Storage & Security Guide

## File-Based H2 with Encryption

Your application now uses **encrypted file-based storage** instead of in-memory storage.

## What Changed

### Before (In-Memory):
```
RAM (temporary)
└── willowsdb ← Data lost when app stops
```

### After (File-Based with Encryption):
```
Your Project Folder
└── willows-rta-portal/
    └── data/
        ├── willowsdb.mv.db ← Your encrypted database file
        └── willowsdb.trace.db ← Debug logs (if errors occur)
```

## How Encryption Works

### Database File Encryption

Your database file is encrypted using **AES encryption** (Advanced Encryption Standard - military-grade security).

**Configuration:**
```properties
spring.datasource.url=jdbc:h2:file:./data/willowsdb;CIPHER=AES
spring.datasource.password=filepassword WillowsRTA2026!Secure
```

**What this means:**
- `CIPHER=AES` = Use AES encryption for the database file
- `filepassword WillowsRTA2026!Secure` = The encryption key
  - `filepassword` = H2 keyword indicating this is the file encryption password
  - `WillowsRTA2026!Secure` = The actual encryption key

### Security Layers

Your application now has **multiple security layers**:

1. **Database File Encryption (AES)**
   - File: `data/willowsdb.mv.db` is encrypted on disk
   - Cannot be opened without the password
   - Even if someone steals the file, they can't read it

2. **User Password Encryption (BCrypt)**
   - All user passwords are hashed using BCrypt
   - Even database admins can't see actual passwords
   - One-way encryption (can't be decrypted)

3. **Spring Security Authentication**
   - Login required to access member data
   - Role-based access control (Admin vs Member)

## Data Persistence

### Where Your Data Lives:

```
willows-rta-portal/
├── src/ (your code)
├── pom.xml
└── data/ ← THIS FOLDER CONTAINS YOUR DATABASE
    └── willowsdb.mv.db ← ENCRYPTED database file
```

### Lifecycle:

1. **First time running:**
   - App creates `data/` folder
   - Creates encrypted `willowsdb.mv.db` file
   - Initializes tables and default admin user

2. **Adding data:**
   - Register members → Saved to encrypted file
   - Stop app → Data remains encrypted on disk

3. **Restart app:**
   - App reads encrypted file using the password
   - All your members are still there! ✅

4. **Backup:**
   - Copy the entire `data/` folder
   - Keep the encryption password safe!

## Security Best Practices

### 🔐 IMPORTANT: Change the Encryption Password!

**Current password (in application.properties):**
```properties
spring.datasource.password=filepassword WillowsRTA2026!Secure
```

**For production, change to something unique:**
```properties
spring.datasource.password=filepassword YourUniqueSecurePassword123!
```

⚠️ **WARNING:** If you change this password after data exists:
- The app won't be able to read the existing database
- You'll need to start fresh OR keep the old password

### 🚫 Never Commit These to Git:

The `.gitignore` file is configured to exclude:
- `data/` folder (contains your database)
- `*.mv.db` (database files)
- Sensitive configuration files

### 📁 Backup Strategy:

**What to backup:**
1. The entire `data/` folder
2. Your encryption password (stored securely, NOT in the backup)

**How often:**
- Daily backups recommended for production
- Before major updates or changes

**Backup script example:**
```bash
# Create timestamped backup
cp -r data/ backups/data-$(date +%Y%m%d-%H%M%S)/
```

## Accessing the Database

### H2 Console Access (Development Only):

1. Start your application
2. Go to: `http://localhost:8082/h2-console`
3. Enter connection details:
   - **JDBC URL:** `jdbc:h2:file:./data/willowsdb;CIPHER=AES`
   - **Username:** `sa`
   - **Password:** `filepassword WillowsRTA2026!Secure`

⚠️ **DISABLE H2 CONSOLE IN PRODUCTION:**
```properties
spring.h2.console.enabled=false
```

## File Size & Performance

### Expected File Sizes:

- **Empty database:** ~50-100 KB
- **With 100 members:** ~200-300 KB
- **With 1000 members:** ~2-3 MB

Very small and efficient! H2 compresses data well.

### Performance:
- Fast read/write operations
- Suitable for up to 10,000+ members
- Single user access (one app instance at a time)

## Migration Path to PostgreSQL/MySQL

When you're ready to scale, here's how to migrate:

### Step 1: Export Your Data

Use H2 Console:
```sql
SCRIPT TO 'backup.sql';
```

### Step 2: Set Up PostgreSQL

Install PostgreSQL, then update `application.properties`:
```properties
# Comment out H2 configuration
# spring.datasource.url=jdbc:h2:file:./data/willowsdb;CIPHER=AES

# Add PostgreSQL configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/willowsdb
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Add PostgreSQL dependency to pom.xml first
```

### Step 3: Import Data

PostgreSQL has built-in encryption options and much better multi-user support.

## Security Comparison

### Current Setup (File-Based H2 with AES):
- ✅ Database file encrypted at rest
- ✅ User passwords encrypted (BCrypt)
- ✅ Data persists between restarts
- ✅ Good for single-location deployment
- ⚠️ Single app instance only
- ⚠️ File-level encryption (whole file or nothing)

### PostgreSQL (Future):
- ✅ Column-level encryption possible
- ✅ Multi-user concurrent access
- ✅ Built-in backup and replication
- ✅ Transparent Data Encryption (TDE)
- ✅ Row-level security
- ✅ Industry standard for production

## Encryption Key Management

### Current Approach:
- Encryption key is in `application.properties`
- Simple but requires file to be kept secure

### Production Recommendations:

1. **Environment Variables:**
```bash
# Set environment variable
export DB_PASSWORD="YourSecurePassword"
```

```properties
# In application.properties
spring.datasource.password=filepassword ${DB_PASSWORD}
```

2. **External Configuration:**
- Store password in separate config file
- Use Spring Cloud Config Server
- Use secrets management (AWS Secrets Manager, HashiCorp Vault)

3. **Access Control:**
- Restrict `application.properties` file permissions
- Only allow app user to read it

```bash
chmod 600 application.properties  # Owner read/write only
```

## Quick Security Checklist

✅ **Before Going Live:**
- [ ] Change database encryption password
- [ ] Change default admin password
- [ ] Disable H2 console (`spring.h2.console.enabled=false`)
- [ ] Set up regular backups
- [ ] Restrict file permissions on `data/` folder
- [ ] Use environment variables for passwords
- [ ] Enable HTTPS (SSL certificate)
- [ ] Set up monitoring and logging

## Questions?

**Q: Can someone decrypt my database if they get the file?**  
A: Not without the encryption password from `application.properties`.

**Q: What if I forget the encryption password?**  
A: You cannot recover the data. Keep secure backups of your password!

**Q: Is AES encryption secure enough?**  
A: Yes! AES is military-grade encryption used by governments and banks.

**Q: Should I use file-based H2 in production?**  
A: For small deployments (< 1000 users), yes. For larger or mission-critical systems, migrate to PostgreSQL.

**Q: How do I backup my data?**  
A: Copy the entire `data/` folder regularly. Store backups securely with the encryption password.

---

**Remember:** Security is a process, not a product. Keep your software updated, use strong passwords, and follow best practices!
