# Quick Reference - Common Settings

Fast lookup for the most frequently changed settings.

---

## ⚡ Most Common Changes

### 1. Enable/Disable Email
**File:** `application.properties`
```properties
app.email.enabled=false  # Development - OTP in logs
app.email.enabled=true   # Production - OTP via email
```

### 2. Enable/Disable MFA
**File:** `application.properties`
```properties
app.mfa.enabled=false  # No OTP required
app.mfa.enabled=true   # OTP required (recommended)
```
⚠️ Requires database reset with H2 when changing

### 3. Email Provider Setup
**File:** `application.properties`

**SendGrid (Recommended):**
```properties
spring.mail.from=noreply@willowsrta.org
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_API_KEY_HERE
app.email.enabled=true
```

**Gmail (Testing):**
```properties
spring.mail.from=your-email@gmail.com
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.email.enabled=true
```

### 4. Change Server Port
**File:** `application.properties`
```properties
server.port=8082  # Default
server.port=8080  # Alternative
```

### 5. Failed Login Settings
**File:** `UserService.java` (line ~107-108)
```java
if (user.getFailedLoginAttempts() >= 5) {  // Max attempts
    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));  // Duration
}
```

### 6. Password Minimum Length
**File:** `PasswordResetController.java` (line ~49)
```java
if (newPassword.length() < 8) {  // Change 8 to desired minimum
```

### 7. OTP Code Settings
**File:** `OtpService.java` (lines 16-17)
```java
private static final int OTP_VALIDITY_MINUTES = 10;  // How long code lasts
private static final int OTP_LENGTH = 6;              // Number of digits
```

### 8. Show SQL Queries (Debug)
**File:** `application.properties`
```properties
spring.jpa.show-sql=true   # Show all SQL queries in logs
spring.jpa.show-sql=false  # Hide SQL (production)
```

---

## 🎯 Environment Presets

### Development Mode
```properties
# Fast testing, no email
app.mfa.enabled=false
app.email.enabled=false
spring.jpa.show-sql=true
server.port=8082
```

### Production Mode
```properties
# Full security
app.mfa.enabled=true
app.email.enabled=true
spring.mail.host=smtp.sendgrid.net
spring.mail.username=apikey
spring.mail.password=${SENDGRID_API_KEY}
spring.jpa.show-sql=false
```

---

## 📂 File Locations Quick Reference

| What | File | Line(s) |
|------|------|---------|
| MFA On/Off | `application.properties` | ~8 |
| Email On/Off | `application.properties` | ~25 |
| Email Provider | `application.properties` | ~10-24 |
| Server Port | `application.properties` | ~4 |
| Failed Login Max | `UserService.java` | ~107 |
| Lockout Duration | `UserService.java` | ~108 |
| Password Min | `PasswordResetController.java` | ~49 |
| OTP Duration | `OtpService.java` | ~16 |
| OTP Length | `OtpService.java` | ~17 |
| App Name | All template files | Header |
| Theme Colors | `style.css` | ~10, ~35, ~78 |

---

## 🔄 After Changing Settings

**Changed in code (.java files)?**
```bash
mvn clean package
mvn spring-boot:run
```

**Changed in properties/templates?**
```bash
# Just restart - no rebuild needed
mvn spring-boot:run
```

**Changed MFA setting?**
```bash
rm -rf data/      # Delete database
mvn spring-boot:run
```

**Deploying to Railway?**
```bash
git add .
git commit -m "Update configuration"
git push origin main
# Railway auto-deploys
```

---

## ⚠️ Important Notes

1. **Never commit secrets** (passwords, API keys) to git
2. **Use environment variables** for production
3. **Backup database** before major config changes
4. **Test locally** before pushing to production
5. **MFA toggle requires database reset** with H2

---

**See `CONFIGURATION.md` for detailed documentation on all settings.**
