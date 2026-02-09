# Troubleshooting Guide

## Common Issues and Solutions

### 🔐 Forced Password Change on First Login

**How it works:**
- When an **admin creates a login** for a member, the system automatically sets `passwordChangeRequired = true`
- On first login, the user is **redirected to change password page**
- User **must change password** before accessing the system
- After password change, the flag is cleared and normal access is granted

**Features:**
- Warning message displayed: "Password Change Required"
- Cancel button hidden (can't skip)
- Security tips shown
- New password must be 8+ characters

---

### 🔄 Switching Between MFA Enabled/Disabled

**The Problem:**
Toggling `app.mfa.enabled` between `true` and `false` can cause database issues with file-based H2.

**Why:**
- When MFA is enabled, the app creates an `otp_codes` table
- When MFA is disabled, the table isn't needed
- H2 file-based database doesn't handle schema changes well

**Solution (Current - H2):**
```bash
# Stop the application
# Delete the database
rm -rf data/

# Restart with your desired MFA setting
mvn spring-boot:run
```

**Solution (Future - PostgreSQL):**
PostgreSQL handles schema changes automatically. No need to delete database!

**Current Workaround:**
Pick one setting and stick with it:
- **Development:** `app.mfa.enabled=false` (easier testing, OTP in logs)
- **Production:** `app.mfa.enabled=true` (security first!)

---

### 🏠 Constitution "Back" Button Logs You Out

**Fixed!** ✅

The constitution page now uses `window.history.back()` instead of linking to home page (`/`).

**What changed:**
```html
<!-- OLD (logged you out) -->
<a href="/">← Back to Home</a>

<!-- NEW (stays logged in) -->
<button onclick="window.history.back()">← Back</button>
```

Now you can view the constitution and return to where you came from without logging out!

---

### 💾 Database Is Read-Only Error

**Error message:**
```
org.h2.jdbc.JdbcSQLNonTransientException: The database is read only
```

**Cause:**
- Database file got corrupted
- File permissions issue
- Disk space full
- Database locked by another process

**Solution:**
```bash
# Stop the application
# Delete the database folder
rm -rf data/

# Restart
mvn spring-boot:run
```

**For Railway:**
```bash
railway run bash
rm -rf data/
exit
# Then redeploy
```

**Prevention:**
Migrate to PostgreSQL (coming soon!) - much more robust.

---

### 🔄 Database Schema Mismatch After Code Update

**Error message:**
```
Column "HAS_USER_ACCOUNT" not found
Column "PASSWORD_CHANGE_REQUIRED" not found
```

**Cause:**
Old database doesn't have new columns we added to the code.

**Solution:**
```bash
# Delete old database
rm -rf data/

# Restart - new database created with correct schema
mvn spring-boot:run
```

**Why this happens:**
- We're using `spring.jpa.hibernate.ddl-auto=update`
- H2 file-based can be finicky with schema changes
- PostgreSQL handles this much better

---

## Moving to PostgreSQL (Recommended)

**Benefits:**
- ✅ No more "read-only database" errors
- ✅ Better schema migration handling
- ✅ Concurrent access support
- ✅ Production-grade reliability
- ✅ Better backup/restore
- ✅ No need to delete database when toggling settings

**When to migrate:**
- When deploying to production
- When you have real member data to protect
- When multiple admins need access
- When you're tired of deleting `data/` folder!

**Coming soon:** PostgreSQL setup guide for Railway

---

## Quick Reference

### Reset Everything (Fresh Start)
```bash
rm -rf data/
mvn clean package
mvn spring-boot:run
```

### Check Database File Permissions
```bash
ls -la data/
# Should show read/write permissions
```

### View Database While Running
```
http://localhost:8082/h2-console
JDBC URL: jdbc:h2:file:./data/willowsdb;CIPHER=AES
Username: sa
Password: filepassword WillowsRTA2026!Secure
```

### Current Known Limitations (H2 File-Based)
1. Can't toggle MFA without database reset
2. Occasional read-only errors
3. Schema changes require database reset
4. Single process access only

### Will Be Fixed by PostgreSQL Migration
All of the above! ✅

---

**Need Help?** Check the logs - they're very detailed and will tell you exactly what's wrong!

---

### 👑 Role Management Issues

**Cannot change role of system administrator**

**Error message:**
```
Cannot change role of system administrator
```

**Cause:**
Trying to change role of the original admin account (username: `admin`)

**Solution:**
This is by design. The system administrator account cannot have its role changed to prevent lockout.

**Workaround:**
1. Create a new admin account by promoting a member
2. Use that account for regular admin work
3. Keep system admin as backup

---

**Change role button not visible**

**Cause 1:** Member has no user account
**Solution:** Create login account first, then change role

**Cause 2:** Viewing system administrator account
**Solution:** System admin role cannot be changed (protected)

**Cause 3:** Not logged in as admin
**Solution:** Only admins can change roles

---

**Promoted user still sees member dashboard**

**Cause:**
User needs to logout and login again for role change to take effect

**Solution:**
1. Tell user to logout
2. Login again
3. Will now see admin dashboard

---

### 🎨 Admin Username Not Showing in Red

**Cause:**
CSS not loaded or browser cache

**Solution:**
1. Hard refresh browser: Ctrl+Shift+R (Windows) or Cmd+Shift+R (Mac)
2. Clear browser cache
3. Check style.css loaded in browser dev tools

---

