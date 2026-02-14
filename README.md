# The Willows RTA Portal

A comprehensive membership portal for "The Willows" Recognised Tenants' Association.

## Features

### Public Features
- ✅ **Member Registration** - Complete online registration form with dual-path option
- ✅ **Self-Registration Toggle** - Members can create accounts or wait for admin approval
- 📋 **Constitution Display** - View constitution online and print/download as PDF
- 🔐 **Secure Login** - Spring Security authentication with optional MFA/OTP

### Member Features
- 👤 **Member Dashboard** - Personal portal for registered members
- 📄 **Profile Access** - View membership details
- 👥 **Members Directory** - Privacy-focused directory of active members
- 📌 **Notice Board** - View announcements and important updates (NEW v1.0.4)
- 🔑 **Change Password** - Self-service password management
- 📋 **Constitution Access** - Always accessible to members

### Admin Features
- 📊 **Admin Dashboard** - Overview of membership statistics
- 👥 **Member Management** - View, edit, and manage all member registrations
- 👁️ **Enhanced Member Directory** - View full contact details including phone, address, login status (NEW v1.0.4)
- ➕ **Add Members Manually** - Add members from paper forms or email
- 📌 **Notice Board Management** - Create, edit, delete, and pin announcements (NEW v1.0.4)
- 👑 **Role Management** - Promote members to admin or demote to member
- 🔐 **Account Security** - Lock/unlock accounts, reset passwords, clear failed login attempts
- 🔧 **Status Management** - Update membership status (Active/Suspended/Terminated)
- 📝 **Member Details** - Complete view of all member information with account status
- 🗑️ **Member Deletion** - Remove members when needed (system admin protected)
- 🔑 **Create Login Accounts** - Generate login credentials for members
- 📊 **Security Dashboard** - View failed login attempts, locked accounts, role assignments

## Technologies Used

- **Java 21**
- **Spring Boot 3.2.1**
  - Spring Web
  - Spring Data JPA
  - **Spring Security** (for authentication)
  - Thymeleaf (Template Engine)
- **PostgreSQL** (production database on Railway)
- **H2 Database** (optional for local development)
- **Resend** (email service for OTP/notifications)
- **Maven** (dependency management)
- **Railway** (cloud hosting platform)

## Security Features

- 🔐 **Two-Factor Authentication (MFA/OTP)** - Optional email-based verification codes
- 🔒 **Failed Login Lockout** - Automatic 15-minute lockout after 5 failed attempts
- 🛡️ **Database Encryption** - AES encryption for database files
- 🔑 **Password Encryption** - BCrypt hashing for all passwords
- 👥 **Role-Based Access Control** - Admin & Member roles with protected routes
- 🔐 **Session Management** - Secure session handling
- 🛡️ **CSRF Protection** - Built-in Spring Security protection
- 🚫 **System Admin Protection** - Original admin account cannot be deleted
- 📧 **Email Uniqueness** - Prevents duplicate member registrations
- 🔓 **Admin Override** - Admins can unlock accounts and reset passwords

**See `DATABASE_SECURITY.md` for complete security documentation.**

## Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://adoptium.net/
   - Verify: `java -version`

2. **Maven** (or use an IDE)
   - Verify: `mvn -version`
   - Download from: https://maven.apache.org/download.cgi

## Quick Start

### Option 1: Command Line

1. **Navigate to the project directory:**
   ```bash
   cd willows-rta-portal
   ```

2. **Build and run:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application:**
   ```
   http://localhost:8082
   ```

### Option 2: Using an IDE

1. Import the project as a Maven project
2. Run `WillowsRtaPortalApplication.java`
3. Open browser to `http://localhost:8082`

## Default Admin Credentials

**⚠️ IMPORTANT: Change these immediately after first login!**

```
Username: admin
Password: admin123
```

## Project Structure

```
willows-rta-portal/
├── src/
│   ├── main/
│   │   ├── java/com/willows/rta/
│   │   │   ├── WillowsRtaPortalApplication.java   # Main application
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java           # Security configuration
│   │   │   │   └── DataInitializer.java          # Creates default admin
│   │   │   ├── controller/
│   │   │   │   ├── PublicController.java         # Public pages
│   │   │   │   ├── AdminController.java          # Admin functions
│   │   │   │   ├── MemberController.java         # Member functions
│   │   │   │   └── DashboardController.java      # Dashboard routing
│   │   │   ├── model/
│   │   │   │   ├── Member.java                   # Member entity
│   │   │   │   └── User.java                     # User/auth entity
│   │   │   ├── repository/
│   │   │   │   ├── MemberRepository.java         # Member database ops
│   │   │   │   └── UserRepository.java           # User database ops
│   │   │   └── service/
│   │   │       ├── MemberService.java            # Member business logic
│   │   │       └── UserService.java              # User/auth logic
│   │   └── resources/
│   │       ├── application.properties             # Configuration
│   │       ├── static/
│   │       │   ├── css/style.css                  # Styles
│   │       │   └── documents/
│   │       │       └── constitution.docx          # Constitution document
│   │       └── templates/
│   │           ├── index.html                     # Home page
│   │           ├── login.html                     # Login page
│   │           ├── register.html                  # Registration form
│   │           ├── constitution.html              # Constitution display
│   │           ├── admin/
│   │           │   ├── dashboard.html             # Admin dashboard
│   │           │   ├── members.html               # Member list
│   │           │   ├── member-details.html        # Member details
│   │           │   └── edit-member.html           # Edit member
│   │           └── member/
│   │               ├── dashboard.html             # Member dashboard
│   │               └── profile.html               # Member profile
└── pom.xml                                        # Dependencies
```

## How to Use

### 1. Public Registration

1. Go to `http://localhost:8082`
2. Click "Register Now"
3. Fill in the membership form with:
   - Full Name
   - Flat/Unit Number
   - Address
   - Email
   - Phone Number
   - Leaseholder Status
   - Preferred Communication Method
   - Consent checkbox
   - Signature (typed name)
4. Submit registration
5. New members will be created but won't have login access until an admin creates their account

### 2. Admin Functions

**Login as Admin:**
- Go to `http://localhost:8082/login`
- Username: `admin`
- Password: `admin123`

**View All Members:**
- Navigate to "Members" from admin dashboard
- See complete list of all registrations

**View Member Details:**
- Click "View" on any member
- See all registration information

**Edit Member:**
- Click "Edit" to modify member details
- Update any information

**Change Status:**
- View member details
- Use status dropdown to change: Active/Suspended/Terminated
- Click "Update Status"

**Delete Member:**
- From members list, click "Delete"
- Confirm deletion

### 3. Member Functions

**Note:** Currently, members can register but committee needs to create login credentials for them. This can be enhanced in future versions.

## Configuration

### Change Port

Edit `src/main/resources/application.properties`:
```properties
server.port=8082  # Change to any available port
```

### Database

Currently using H2 (in-memory). To use PostgreSQL or MySQL:

1. Add dependency to `pom.xml`
2. Update `application.properties` with database connection details

### H2 Database Console

Access at: `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:mem:willowsdb`
- Username: `sa`
- Password: (leave empty)

## Security Notes

### Password Management

- Passwords are encrypted using BCrypt
- Never store plain-text passwords
- Default admin password MUST be changed immediately

### Creating New Admin Users

Currently done through code. To add in `DataInitializer.java`:
```java
userService.createUser("newadmin", "password", "ROLE_ADMIN");
```

### Creating Member Login Accounts

Admin can create accounts for registered members (feature to be added in UI).

## Customization Ideas

### Immediate Enhancements
1. **Add email notifications** - Send confirmation emails on registration
2. **Password reset** - Allow users to reset forgotten passwords
3. **Member self-service** - Let members create their own login after registration
4. **Document library** - Add more documents beyond constitution
5. **Meeting minutes** - Upload and display meeting records

### Future Features
1. **News/Announcements** - Post updates for members
2. **Event Calendar** - Schedule and RSVP for meetings
3. **Polls/Voting** - Online voting for decisions
4. **Document uploads** - Members upload documents
5. **Discussion forum** - Member communication
6. **Committee positions** - Track chairperson, secretary, treasurer

## Troubleshooting

**Port 8082 already in use:**
- Change port in `application.properties`

**Application won't start:**
- Check Java version: `java -version` (needs 17+)
- Run: `mvn clean install` then `mvn spring-boot:run`

**Can't login:**
- Verify you're using correct credentials
- Check H2 console to see if users exist

**Registration fails:**
- Check for duplicate email addresses
- Ensure all required fields are filled

## Data Privacy & GDPR

This application collects personal data. Ensure compliance with:
- UK GDPR regulations
- Data Protection Act 2018
- Obtain proper consent (included in registration form)
- Have a data retention policy
- Provide data access/deletion upon request

## Deployment

For production deployment:

1. **Use a real database** (PostgreSQL recommended)
2. **Change to production mode** in `application.properties`
3. **Use HTTPS** (SSL certificate required)
4. **Change all default passwords**
5. **Set up backup procedures**
6. **Configure proper logging**
7. **Consider hosting options:**
   - Heroku
   - AWS Elastic Beanstalk
   - DigitalOcean
   - Your own server

## Support & Maintenance

### Regular Maintenance
- Back up database regularly
- Update member statuses as needed
- Review and approve new registrations
- Monitor for inactive accounts

### Security Updates
- Keep Spring Boot updated
- Monitor security advisories
- Change default passwords
- Review access logs

## License

This is a custom application built for The Willows Recognised Tenants' Association.

---

**Built for The Willows RTA** 🏘️  
*Empowering residents through technology*
