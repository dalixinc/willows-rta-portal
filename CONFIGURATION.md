# Configuration Guide - The Willows RTA Portal

Complete guide to all configurable options and settings in the application.

---

## 📧 Email Configuration

### Location: `src/main/resources/application.properties`

### Email Sender Address
```properties
# From email address (used as sender for OTP emails)
spring.mail.from=noreply@willowsrta.org
```
**What it does:** Sets the "From" address on all system emails (OTP codes, welcome emails)  
**Default:** `noreply@willowsrta.org`  
**Change to:** Your organization's email address

---

### Email Provider Options

#### Option 1: Gmail (Development/Testing)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.email.enabled=true
```
**Limits:** 500 emails/day  
**Best for:** Testing, small organizations  
**Setup:** Requires Google App Password (not regular password)

#### Option 2: SendGrid (Recommended for Production)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY
app.email.enabled=true
```
**Limits:** 100 emails/day (free tier)  
**Best for:** Production, reliable delivery  
**Setup:** Sign up at sendgrid.com, create API key

#### Option 3: AWS SES (High Volume)
```properties
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=YOUR_SMTP_USERNAME
spring.mail.password=YOUR_SMTP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.email.enabled=true
```
**Limits:** Pay per use  
**Best for:** Enterprise, high volume  
**Setup:** AWS account, verify domain

#### Development Mode (No Email)
```properties
app.email.enabled=false
```
**What happens:** OTP codes printed to console/logs instead of emailed  
**Best for:** Development, testing without email setup

---

## 🔐 Multi-Factor Authentication (MFA)

### Location: `src/main/resources/application.properties`

```properties
# Enable/disable two-factor authentication
app.mfa.enabled=true
```

**Options:**
- `true` = MFA enabled (secure, recommended for production)
- `false` = MFA disabled (faster login, development only)

**When `true`:**
- Login requires username + password + OTP code
- OTP sent via email (or logged to console if email disabled)
- Code expires in 10 minutes

**When `false`:**
- Login requires only username + password
- No OTP required
- Faster testing during development

**⚠️ Important:** Changing this requires database reset with H2 (will be fixed with PostgreSQL)

---

## 👥 Self-Registration Control

### Location: `src/main/resources/application.properties`

```properties
# Enable/disable self-registration (members creating own login accounts)
app.self-registration.enabled=true
```

**Options:**
- `true` = Members can create login accounts during registration (open)
- `false` = Only admins can create accounts (requires approval)

**When `true` (Open Registration):**
- Registration page shows "Account Setup" option
- Member can choose to create account immediately
- Or wait for admin to create it later
- Faster onboarding

**When `false` (Admin Approval):**
- Registration page shows admin review notice
- Member only submits application
- No account creation option shown
- Admin must create all accounts manually
- Better for security/vetting

**Use `true` when:**
- You trust new members
- Want quick onboarding
- Small, tight-knit community

**Use `false` when:**
- Need to vet new members
- Require approval process
- High-security requirements
- Want to verify identity first

---

## 🔒 Login Security Settings

### Location: `src/main/java/com/willows/rta/service/UserService.java`

### Failed Login Lockout

**Current Settings:**
```java
// Lock account after 5 failed attempts
if (user.getFailedLoginAttempts() >= 5) {
    
// Lock duration: 15 minutes
user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
```

**Configurable Options:**

#### Maximum Failed Attempts (Line ~107)
```java
if (user.getFailedLoginAttempts() >= 5) {  // ← Change this number
```
**Common values:**
- `3` = Strict (good for high-security)
- `5` = Balanced (current, recommended)
- `10` = Lenient (user-friendly)

#### Lockout Duration (Line ~108)
```java
user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));  // ← Change duration
```
**Common values:**
- `.plusMinutes(10)` = 10 minutes
- `.plusMinutes(15)` = 15 minutes (current)
- `.plusMinutes(30)` = 30 minutes
- `.plusHours(1)` = 1 hour

#### Warning Threshold (Line ~143 in AuthController)
```java
if (remainingAttempts > 0 && remainingAttempts <= 3) {
```
**What it does:** Shows "X attempts remaining" warning  
**Current:** Warns when 3 or fewer attempts left  
**Change to:** Show warnings earlier or later

---

## 🔑 Password Requirements

### Location: `src/main/java/com/willows/rta/controller/PasswordResetController.java`

### Minimum Password Length
```java
// Validate password length (Line ~49)
if (newPassword.length() < 8) {
```
**Current:** 8 characters minimum  
**Change to:** Any number (10, 12, 16, etc.)

### Password Validation
Currently enforces:
- ✅ Minimum 8 characters
- ✅ Must match confirmation

**To add complexity requirements**, modify line ~49:
```java
// Example: Require uppercase, lowercase, number
if (newPassword.length() < 8 || 
    !newPassword.matches(".*[A-Z].*") || 
    !newPassword.matches(".*[a-z].*") || 
    !newPassword.matches(".*[0-9].*")) {
    
    redirectAttributes.addFlashAttribute("error", 
        "Password must be 8+ characters with uppercase, lowercase, and number");
    return "redirect:/member/change-password";
}
```

---

## 🕐 OTP/MFA Settings

### Location: `src/main/java/com/willows/rta/service/OtpService.java`

### OTP Code Length
```java
private static final int OTP_LENGTH = 6;  // Line ~17
```
**Current:** 6 digits  
**Change to:** 4, 6, or 8 digits

### OTP Validity Duration
```java
private static final int OTP_VALIDITY_MINUTES = 10;  // Line ~16
```
**Current:** 10 minutes  
**Change to:** Any number of minutes (5, 10, 15, 30)

### OTP Generation Method
```java
// Line ~72 - generates numeric code
int code = 100000 + random.nextInt(900000);
```
**Current:** 6-digit numeric (100000-999999)  
**Alternative:** Alphanumeric codes (would need different logic)

---

## 💾 Database Configuration

### Location: `src/main/resources/application.properties`

### Current: H2 File-Based Database
```properties
# Database Connection
spring.datasource.url=jdbc:h2:file:./data/willowsdb;CIPHER=AES
spring.datasource.username=sa
spring.datasource.password=filepassword WillowsRTA2026!Secure
spring.datasource.driver-class-name=org.h2.Driver

# JPA/Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

**Options:**

#### Development (Show SQL queries)
```properties
spring.jpa.show-sql=true
```

#### Reset Database on Startup (⚠️ DELETES ALL DATA)
```properties
spring.jpa.hibernate.ddl-auto=create
```

#### Production (No automatic changes)
```properties
spring.jpa.hibernate.ddl-auto=validate
```

---

### Future: PostgreSQL Configuration

```properties
# PostgreSQL Connection (for production)
spring.datasource.url=jdbc:postgresql://localhost:5432/willowsrta
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Settings
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

**Benefits:**
- No more database file corruption
- Handle MFA toggle without reset
- Better concurrent access
- Production-grade reliability

---

## 🎨 User Interface Settings

### Application Name
**Locations:** Multiple template files

**Change in all these files:**
```html
<h1>🏘️ The Willows RTA</h1>
```
**Files to update:**
- `templates/index.html`
- `templates/login.html`
- `templates/register.html`
- `templates/constitution.html`
- `templates/admin/dashboard.html`
- `templates/member/dashboard.html`
- All other templates with header

### Colors/Theme
**Location:** `src/main/resources/static/css/style.css`

#### Primary Color (Green)
```css
/* Line ~10 - Background gradient */
background: linear-gradient(135deg, #2E7D32 0%, #66BB6A 100%);

/* Line ~35 - Headers */
color: #2E7D32;

/* Line ~78 - Active nav items */
background: #2E7D32;
```

**To change theme:**
Replace `#2E7D32` (dark green) and `#66BB6A` (light green) with your colors

#### Header Font Sizes
```css
/* Line ~42 */
.site-header h1 {
    font-size: 2.2em;  /* Main title */
}

.site-header h2 {
    font-size: 1.2em;  /* Subtitle */
}
```

---

## 🔧 Server Configuration

### Location: `src/main/resources/application.properties`

### Port Number
```properties
server.port=8082
```
**Default:** 8082  
**Change to:** Any available port (8080, 8081, 3000, etc.)

### File Upload Limits
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```
**Default:** 10MB  
**Change to:** Larger if needed (50MB, 100MB, etc.)

### Session Timeout
**Not currently configured - uses default (30 minutes)**

To add:
```properties
server.servlet.session.timeout=30m
```
**Options:** `15m`, `30m`, `1h`, `2h`

---

## 👥 Member Registration

### Location: `templates/register.html`

### Default Membership Status
**Location:** `PublicController.java` (line ~53)
```java
member.setMembershipStatus("ACTIVE");
```
**Options:**
- `"ACTIVE"` - Immediate membership (current)
- `"PENDING"` - Requires admin approval

### Required Fields
**Location:** `Member.java` model

**Current required fields:**
- Full name (`@NotBlank`)
- Email (`@NotBlank`, `@Email`)
- Address (`@NotBlank`)
- Flat number (`@NotBlank`)

**To make field optional:**
Remove `@NotBlank` annotation

**To add new required field:**
Add `@NotBlank` annotation

---

## 📧 Admin Notifications

### Currently NOT Implemented

**Future options to add:**
- Email admin when new member registers
- Email admin when account is locked
- Email admin when password is reset

**Would require:** Additional email templates and service methods

---

## 🔐 Security Headers

### Currently NOT Configured

**To add security headers**, modify `SecurityConfig.java`:
```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .frameOptions(frame -> frame.deny())
);
```

---

## 📊 Logging Configuration

### Location: `src/main/resources/application.properties`

**Currently:** Default Spring Boot logging

**To customize:**
```properties
# Log level options: TRACE, DEBUG, INFO, WARN, ERROR
logging.level.root=INFO
logging.level.com.willows.rta=DEBUG
logging.level.org.springframework.security=DEBUG

# Log file location
logging.file.name=logs/application.log
```

---

## 🌍 Internationalization (i18n)

### Currently NOT Implemented

**Language:** English only

**To add multiple languages**, would need:
- Message properties files
- Locale configuration
- Template updates

---

## ⚙️ Environment-Specific Configuration

### Using Environment Variables (Recommended for Production)

**Location:** Railway dashboard or `.env` file

**Example:**
```properties
# In application.properties, use:
spring.mail.password=${SMTP_PASSWORD}
app.mfa.enabled=${MFA_ENABLED:true}
spring.datasource.password=${DB_PASSWORD}

# Then set in Railway:
SMTP_PASSWORD=your-secret-key
MFA_ENABLED=true
DB_PASSWORD=your-db-password
```

**Benefits:**
- Secrets not in code
- Different settings per environment
- Easy to change without code deploy

---

## 🎯 Quick Reference Table

| Setting | Location | Default | Production Recommended |
|---------|----------|---------|----------------------|
| **Email From Address** | `application.properties` | `noreply@willowsrta.org` | Your domain |
| **Email Provider** | `application.properties` | Disabled | SendGrid |
| **MFA Enabled** | `application.properties` | `true` | `true` |
| **Failed Login Attempts** | `UserService.java` | 5 | 5 |
| **Lockout Duration** | `UserService.java` | 15 min | 15-30 min |
| **OTP Validity** | `OtpService.java` | 10 min | 10 min |
| **Password Min Length** | `PasswordResetController.java` | 8 chars | 10-12 chars |
| **Server Port** | `application.properties` | 8082 | Any |
| **Database** | `application.properties` | H2 file | PostgreSQL |

---

## 🔄 Common Configuration Scenarios

### Scenario 1: Development (Fast Testing)
```properties
app.mfa.enabled=false
app.email.enabled=false
spring.jpa.show-sql=true
server.port=8082
```

### Scenario 2: Staging (Testing with Email)
```properties
app.mfa.enabled=true
app.email.enabled=true
spring.mail.host=smtp.sendgrid.net
# ... SendGrid credentials
spring.jpa.show-sql=false
```

### Scenario 3: Production (Full Security)
```properties
app.mfa.enabled=true
app.email.enabled=true
spring.mail.host=smtp.sendgrid.net
# ... SendGrid credentials via environment variables
spring.datasource.url=${DATABASE_URL}  # PostgreSQL
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
```

---

## 📝 Configuration Checklist

### Before Going Live:

- [ ] Configure email provider (SendGrid recommended)
- [ ] Set `app.email.enabled=true`
- [ ] Keep `app.mfa.enabled=true`
- [ ] Use environment variables for secrets
- [ ] Migrate to PostgreSQL
- [ ] Increase password minimum to 10+ characters
- [ ] Set `spring.jpa.show-sql=false`
- [ ] Test failed login lockout
- [ ] Test password reset flow
- [ ] Test MFA with real emails
- [ ] Set up database backups
- [ ] Configure custom domain for email

---

## 🆘 Need Help?

See `TROUBLESHOOTING.md` for common issues and solutions.

For questions about configuration, check:
1. This guide
2. `README.md`
3. `EMAIL_SETUP.md`
4. Code comments in relevant files

---

**Last Updated:** February 2026  
**Version:** 1.0
