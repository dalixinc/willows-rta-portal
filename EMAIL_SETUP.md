# Email Configuration Guide for OTP/MFA

## 🔐 Two-Factor Authentication (2FA) with OTP

Your application now includes **email-based two-factor authentication** for enhanced security. When users log in, they must enter a 6-digit code sent to their email.

---

## 📧 Email Setup Options

### Development Mode (Current Setup - NO EMAIL CONFIGURED)

**How it works now:**
- Email is **disabled** by default
- OTP codes are **printed to the console/logs** instead
- Perfect for testing and development

**To use:**
1. Start your application
2. Login normally
3. **Check the console/logs** for the OTP code
4. Enter the code on the verification page

**Console output looks like:**
```
========================================
EMAIL NOT CONFIGURED - OTP CODE:
To: member@example.com
OTP Code: 123456
This code will expire in 10 minutes
========================================
```

---

## 🚀 Production Setup - Email Options

### Option 1: Gmail (Easy for Testing)

**Steps:**
1. Create a Gmail account (or use existing)
2. Enable 2-Step Verification on your Google Account
3. Generate an "App Password":
   - Go to: https://myaccount.google.com/apppasswords
   - Select "Mail" and your device
   - Copy the 16-character password

4. Update `application.properties`:
```properties
# Gmail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Enable email
app.email.enabled=true
```

**Pros:** Free, easy to set up  
**Cons:** Gmail limits (500 emails/day), not ideal for production

---

### Option 2: SendGrid (Recommended for Production)

**Why SendGrid:**
- ✅ Free tier: 100 emails/day
- ✅ Professional delivery
- ✅ Good deliverability rates
- ✅ Easy to scale

**Steps:**
1. Sign up at: https://sendgrid.com
2. Verify your email
3. Create an API Key:
   - Settings → API Keys → Create API Key
   - Give it "Mail Send" permissions
   - Copy the API key

4. Update `application.properties`:
```properties
# SendGrid Configuration
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY_HERE

# Enable email
app.email.enabled=true
```

---

### Option 3: AWS SES (For High Volume)

**Best for:**
- High email volume
- Existing AWS infrastructure

**Steps:**
1. Set up AWS SES in your region
2. Verify your domain or email
3. Get SMTP credentials
4. Update `application.properties`:
```properties
# AWS SES Configuration
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=YOUR_SMTP_USERNAME
spring.mail.password=YOUR_SMTP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.email.enabled=true
```

---

### Option 4: Other Providers

**Also supported:**
- Mailgun
- Postmark
- Amazon SES
- SMTP2GO
- Any SMTP server

Just update the SMTP settings accordingly.

---

## 🔧 Configuration for Railway/Production

### Using Environment Variables (Recommended):

Instead of hardcoding credentials in `application.properties`, use environment variables:

**In `application.properties`:**
```properties
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.email.enabled=${EMAIL_ENABLED:false}
```

**In Railway Dashboard:**
1. Go to your project
2. Variables tab
3. Add:
   - `SMTP_HOST` = smtp.sendgrid.net
   - `SMTP_PORT` = 587
   - `SMTP_USERNAME` = apikey
   - `SMTP_PASSWORD` = your-sendgrid-api-key
   - `EMAIL_ENABLED` = true

---

## 🧪 Testing Email Setup

### Test 1: Application Startup
```bash
mvn spring-boot:run
```

Check console for errors. Should see:
```
Started WillowsRtaPortalApplication in X seconds
```

### Test 2: Login Flow
1. Go to login page
2. Enter username/password
3. Submit
4. **Check console** (if email disabled) or **check email** (if enabled)
5. Enter OTP code
6. Should login successfully

### Test 3: Email Delivery
- Try logging in with a test account
- Check spam folder if email doesn't arrive
- Verify the "from" address isn't blacklisted

---

## 📊 How OTP Works

### Login Flow:
```
1. User enters username + password
   ↓
2. System validates credentials
   ↓
3. System generates random 6-digit code
   ↓
4. Code saved to database (expires in 10 minutes)
   ↓
5. Email sent with OTP code
   ↓
6. User enters code
   ↓
7. System validates code
   ↓
8. Login successful!
```

### Security Features:
- ✅ Codes expire after 10 minutes
- ✅ Codes can only be used once
- ✅ New code invalidates old codes
- ✅ Session timeout after 10 minutes
- ✅ Email address is masked (us***@example.com)

---

## 🔒 Security Best Practices

### Email Configuration:
1. **Never commit credentials** to git
2. **Use environment variables** in production
3. **Use app-specific passwords** (not main password)
4. **Enable SPF/DKIM** for your domain (prevents spam)
5. **Monitor email quotas**

### OTP Security:
1. **Short expiry time** (10 minutes)
2. **One-time use** only
3. **Logged for audit**
4. **Rate limiting** (can add later)

---

## 🐛 Troubleshooting

### "Email not configured" messages in console
✅ **This is normal in development!** Codes are printed to console.

### Emails not arriving
1. Check spam folder
2. Verify SMTP credentials
3. Check email quota (SendGrid free = 100/day)
4. Check console logs for errors
5. Verify `app.email.enabled=true`

### "Invalid SMTP credentials"
- Double-check username/password
- For Gmail: use app password, not main password
- For SendGrid: username must be "apikey"

### Emails going to spam
1. Set up SPF records for your domain
2. Set up DKIM
3. Use a verified sender domain
4. Avoid spam trigger words

---

## 📈 Monitoring

### Check Email Logs:
Application logs will show:
```
OTP email sent successfully to: user@example.com
```

Or:
```
Failed to send email to user@example.com: [error]
```

### SendGrid Dashboard:
- View sent emails
- Track deliverability
- Monitor quota

---

## 🎯 Next Steps

### Immediate (Development):
1. ✅ Use console output for OTP codes
2. ✅ Test login flow
3. ✅ Verify OTP validation works

### Before Production:
1. Set up SendGrid account
2. Configure environment variables
3. Test email delivery
4. Set `app.email.enabled=true`
5. Monitor first few logins

### Optional Enhancements:
1. Add rate limiting (prevent spam)
2. Add email templates (HTML emails)
3. Add SMS OTP as alternative
4. Add "remember device" feature
5. Add admin OTP bypass option

---

## 📝 Quick Reference

### Development (Console OTP):
```properties
app.email.enabled=false
```
OTP codes print to console ✅

### Production (SendGrid):
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_API_KEY
app.email.enabled=true
```
OTP codes sent via email ✅

---

## 💡 Pro Tips

1. **Start with console output** - Test the flow before configuring email
2. **Use SendGrid free tier** - Perfect for small organizations
3. **Set up environment variables** - Never commit credentials
4. **Monitor deliverability** - Check SendGrid dashboard regularly
5. **Have a backup plan** - Keep admin console access in case of email issues

---

**Need help?** Check the logs, they're very verbose and will tell you exactly what's happening with email sending!
