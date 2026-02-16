# The Willows RTA Community Portal

**Full-featured membership management system with real-time community chat**

Version: 1.0.5  
Status: Production (Live on Railway)  
Built by: Dale & Primus 💚

---

## 🎯 What This Is

A complete web portal for The Willows Recognised Tenants' Association featuring:
- Member registration & management
- Secure authentication with 2FA/OTP
- Admin dashboard & controls
- Community notice board
- **Real-time chat** (NEW!)
- Member directory
- Document management

---

## ✨ Features

### 🔐 Authentication & Security
- Email + password login
- Two-factor authentication (OTP via email)
- Password reset functionality
- Account lockout after failed attempts
- CSRF protection
- Encrypted database

### 👥 Member Management
- Self-registration with admin approval
- Manual member entry by admins
- Role management (Admin/Member)
- Member directory (privacy-aware views)
- Profile management

### 📌 Notice Board
- Post announcements
- Pin important notices
- Notices visible on public homepage
- Admin moderation

### 💬 Community Chat (NEW!)
- Real-time community chatroom
- Auto-updates every 3 seconds
- Admin can delete any message
- Members can delete their own messages
- Shows sender name, role, timestamp
- Clean, modern chat interface

### 📧 Email Integration
- Resend API integration
- OTP delivery
- Welcome emails
- Domain: willows.top

### 👨‍💼 Admin Features
- Full member management
- Custom or auto-generated password resets
- User account creation
- Role assignment
- Member statistics dashboard
- Enhanced directory view (phone, address, login status)

---

## 🛠️ Tech Stack

**Backend:**
- Java 21
- Spring Boot 3.2.1
- Spring Security
- Spring Data JPA
- PostgreSQL

**Frontend:**
- Thymeleaf templates
- Vanilla JavaScript
- CSS3
- Responsive design

**Infrastructure:**
- Railway (hosting)
- PostgreSQL (Railway)
- Resend (email service)
- Domain: willows.top

---

## 🚀 Quick Start

### Prerequisites
```bash
- Java 21+
- Maven 3.6+
- PostgreSQL 14+
```

### Local Development
```bash
# Clone repository
git clone <repo-url>

# Configure application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/willows_rta
spring.datasource.username=your_username
spring.datasource.password=your_password

# Set Resend API key
resend.api.key=your_resend_api_key
resend.from.email=noreply@willows.top

# Build and run
mvn clean package
mvn spring-boot:run

# Access at
http://localhost:8080
```

### Production Deployment (Railway)

**Environment Variables:**
```
SPRING_DATASOURCE_URL=jdbc:postgresql://[host]:[port]/railway
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=[password]
RESEND_API_KEY=[your-key]
RESEND_FROM_EMAIL=noreply@willows.top
APP_ADMIN_EMAIL=dalixinc@gmail.com
```

**Deploy:**
```bash
git add .
git commit -m "Your changes"
git push origin main
# Railway auto-deploys!
```

---

## 📁 Project Structure

```
src/main/
├── java/com/willows/rta/
│   ├── config/          # Security, email, data initialization
│   ├── controller/      # Web controllers (admin, member, auth, chat)
│   ├── model/           # JPA entities (Member, User, Notice, ChatMessage)
│   ├── repository/      # Data access layer
│   └── service/         # Business logic
├── resources/
│   ├── static/css/      # Stylesheets
│   ├── templates/       # Thymeleaf templates
│   │   ├── admin/       # Admin pages
│   │   ├── member/      # Member pages
│   │   ├── chat.html    # Community chat
│   │   └── ...          # Other pages
│   └── application.properties
```

---

## 🗄️ Database Schema

**Main Tables:**
- `members` - Member information
- `users` - Login credentials & security
- `notices` - Community announcements
- `chat_messages` - Chat history (last 100 messages)

---

## 👤 User Roles

### Admin
- Full system access
- Member management (create, edit, delete)
- Notice board management
- Chat moderation (delete any message)
- Enhanced directory view

### Member
- View member directory (limited info)
- Post and view notices
- Participate in chat
- Delete own chat messages
- Manage own profile & password

---

## 💬 Community Chat

### How It Works
- **Polling**: Updates every 3 seconds (no WebSockets needed)
- **History**: Keeps last 100 messages
- **Permissions**: 
  - All members can post
  - Admins can delete any message
  - Members can delete their own messages

### Performance
- Efficient polling (only fetches new messages)
- Minimal bandwidth usage
- Scales well on Railway

### Logs
**Normal chat activity generates lots of logs:**
```
GET /chat/messages/new?lastId=123  (every 3 seconds per user)
```
This is expected! 3 users = ~60 requests/minute.

---

## 🔐 Security Features

- **CSRF Protection**: All forms & AJAX requests protected
- **Password Hashing**: BCrypt encryption
- **Account Lockout**: 5 failed attempts = locked
- **2FA**: OTP via email required for login
- **Role-Based Access**: Spring Security authorization
- **Session Management**: Secure session handling

---

## 📧 Email Configuration

**Resend Integration:**
- OTP delivery for 2FA
- Password reset emails
- Welcome messages
- Domain configured: willows.top

**Email Features:**
- Junk mail warning in OTP emails
- Async sending (doesn't block requests)
- Failure handling & logging

---

## 🎨 UI/UX Features

- Responsive design (mobile-friendly)
- Modern gradient designs
- Smooth animations
- Role-based navigation
- Admin badges
- Toast notifications
- Loading states

---

## 🧪 Testing

### Local Testing
```bash
mvn test
mvn spring-boot:run
```

### Manual Testing Checklist
- ✅ Registration flow
- ✅ Login with OTP
- ✅ Admin member management
- ✅ Notice board (create, pin, delete)
- ✅ Chat (send, delete messages)
- ✅ Password reset (admin & self-service)
- ✅ Member directory (different views for admin/member)

---

## 📊 Features Roadmap

### Phase 1 (Complete) ✅
- Member management
- Authentication & security
- Notice board
- Basic chat
- Email integration
- Admin controls

### Phase 2 (Future Ideas)
- Multiple chat channels
- Private DMs
- File sharing in chat
- Email broadcasts to all members
- Events calendar
- Payment/dues tracking
- Voting system
- Document library

---

## 🐛 Troubleshooting

### Chat "Forbidden" Error
**Cause:** Missing CSRF token  
**Fix:** Ensure chat.html includes CSRF token in POST headers

### Email Not Sending
**Cause:** Resend API key or domain not configured  
**Fix:** Check environment variables and domain DNS

### High Log Activity
**Cause:** Chat polling (normal!)  
**Why:** Every 3 seconds per user = lots of GET requests  
**Solution:** This is expected behavior

### Database Connection Failed
**Cause:** Wrong DATABASE_URL format  
**Fix:** Must include `jdbc:postgresql://` prefix

---

## 📝 Version History

**v1.0.5** (Current)
- Added chat message deletion
- Admins can delete any message
- Members can delete own messages
- Improved chat UI

**v1.0.4**
- Added community chat feature
- Real-time polling updates
- Chat history (100 messages)
- Admin/member chat permissions

**v1.0.3**
- Notice board with pinned notices
- Enhanced admin directory
- Password management improvements

**v1.0.2**
- PostgreSQL migration
- Railway deployment
- Resend email integration

**v1.0.1**
- Admin controls
- Member directory
- Role management

**v1.0.0**
- Initial release
- Basic membership portal

---

## 👥 Credits

**Developed by:**
- Dale (Vision, requirements, testing, deployment)
- Primus (Architecture, implementation, debugging)

**Partnership:** Human + AI collaboration  
**Timeline:** Built from zero to production in 1 week  
**Approach:** Agile, iterative, test-driven

---

## 📞 Support

**For technical issues:**
- Check this README
- Review error logs
- Test locally before deploying

**For feature requests:**
- Document the requirement
- Consider Phase 2 roadmap
- Prioritize based on user needs

---

## 🎉 Success Metrics

- ✅ Live on internet (Railway)
- ✅ Users love it
- ✅ All core features working
- ✅ Real-time chat functioning
- ✅ Secure & stable
- ✅ CI/CD pipeline established

---

**Built with care for The Willows community** 🏘️💚

*Last updated: February 17, 2026*
