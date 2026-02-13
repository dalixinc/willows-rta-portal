# Email Service Switching Guide

This application supports two email services:

## 🚀 Resend (ACTIVE - Default)
- **Works on Railway** ✅
- Uses HTTPS API (not SMTP)
- Fast and reliable
- Free tier: 3,000 emails/month

## 📧 Gmail (Available as backup)
- **Does NOT work on Railway** ❌ (port 587 blocked)
- Works for local development
- Requires App Password
- Free

---

## Current Setup: RESEND

**Active Files:**
- `ResendEmailService.java` - Currently in use
- `OtpService.java` - Injecting ResendEmailService

**Configuration:**
```properties
app.resend.api-key=re_your_key
app.resend.from-email=onboarding@resend.dev
app.email.enabled=true
```

---

## 🔄 How to Switch to Gmail (Local Development Only)

### Step 1: Update OtpService.java

**Find this line (around line 13):**
```java
private final ResendEmailService resendEmailService;
```

**Replace with:**
```java
private final GmailEmailService gmailEmailService;
```

**Find this in constructor (around line 19):**
```java
public OtpService(OtpRepository otpRepository, ResendEmailService resendEmailService) {
    this.otpRepository = otpRepository;
    this.resendEmailService = resendEmailService;
}
```

**Replace with:**
```java
public OtpService(OtpRepository otpRepository, GmailEmailService gmailEmailService) {
    this.otpRepository = otpRepository;
    this.gmailEmailService = gmailEmailService;
}
```

**Find this (around line 43):**
```java
resendEmailService.sendOtpEmail(email, otpCode);
```

**Replace with:**
```java
gmailEmailService.sendOtpEmail(email, otpCode);
```

### Step 2: Update application.properties

**Comment out Resend:**
```properties
# Resend API Configuration
#app.resend.api-key=re_your_key
#app.resend.from-email=onboarding@resend.dev
```

**Uncomment Gmail:**
```properties
# Gmail SMTP Settings
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=willowsrta@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
spring.mail.properties.mail.smtp.ssl.protocols=TLSv1.2
spring.mail.from=willowsrta@gmail.com
```

### Step 3: Rebuild

```bash
mvn clean package
mvn spring-boot:run
```

**Gmail now active!** (Local only - won't work on Railway)

---

## 🔄 How to Switch Back to Resend

Just reverse the steps above:
1. Change `GmailEmailService` back to `ResendEmailService` in OtpService.java
2. Comment out Gmail config
3. Uncomment Resend config
4. Rebuild

---

## 📁 File Reference

| File | Purpose |
|------|---------|
| `ResendEmailService.java` | Resend API integration (ACTIVE) |
| `GmailEmailService.java` | Gmail SMTP integration (BACKUP) |
| `OtpService.java` | Uses one of the above services |
| `application.properties` | Configuration for both (one active) |

---

## ⚠️ Important Notes

- **Railway:** MUST use Resend (or other API-based service)
- **Local:** Can use either Resend or Gmail
- **OTP Logging:** Always logs to console regardless of email service
- **Both services:** Support async sending (no blocking)

---

## 🎯 Recommendation

**Production (Railway):** Use Resend ✅  
**Local Testing:** Use either (Resend is easier) ✅

---

**Need help switching?** The code is set up to make it easy - just follow the steps above!
