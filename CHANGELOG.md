# Changelog - The Willows RTA Portal

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased]

### Planned Features
- PostgreSQL database migration
- Admin email notifications
- Batch member import (CSV)
- Member profile photo upload
- Meeting minutes management
- Document library
- SMS OTP as alternative to email

---

## [1.0.2] - 2026-02-09

### Added
- **Role Management System**: Admins can promote members to admin or demote to member
- **Role Column in Members List**: Shows Admin/Member role with color-coded badges
- **Admin Username Styling**: Admin usernames displayed in red (members in black)
- **System Admin Protection**: Original admin account cannot be deleted or have role changed
- **Visual Role Indicators**: Red badge for Admin, green badge for Member

**Features:**
- Change member role: Admin ↔ Member (one-click toggle)
- System admin flagging (`systemAdmin` field in database)
- Role displayed in members list and member details
- Confirmation dialogs for role changes
- Protection prevents deletion/role change of system admin

**Files Modified:**
- `User.java` - Added `systemAdmin` boolean field
- `Member.java` - Added `userRole` transient field
- `DataInitializer.java` - Mark default admin as system admin
- `AdminController.java` - Added toggle role endpoint, populate userRole in list
- `admin/members.html` - Added Role column
- `admin/member-details.html` - Added role display and change role button
- `admin/dashboard.html` - Added admin username styling
- `style.css` - Added `.admin-username` and `.badge-admin` styles

**UI Changes:**
- Members list now has "Role" column
- Admin usernames appear in red
- Member usernames appear in black
- Role badges: Red (Admin), Green (Member)
- Change role button in member details
- System admin badge for protected account

---

## [1.0.1] - 2026-02-09

### Fixed
- **Login Lockout Timing**: Account now properly locks for 15 minutes on 5th failed attempt (not before)
- Lockout check now happens AFTER recording failed attempt, not before
- User object refreshed after recording attempt to get correct lock status

### Added
- **Self-Registration Toggle**: Admins can control whether members can create own accounts
- **Account Status Column**: Members list now shows account lock status
- Account status indicators: 🔒 Locked, ⏱️ Temp Lock, X fails, Active

**Configuration:**
```properties
app.self-registration.enabled=true  # Members can create accounts
app.self-registration.enabled=false # Only admins create accounts
```

**Files Modified:**
- `application.properties` - Added self-registration toggle
- `PublicController.java` - Added toggle check and validation
- `register.html` - Conditional display of account creation
- `AdminController.java` - Populate user status in members list
- `Member.java` - Added transient fields for user status display
- `admin/members.html` - Added Account Status column
- `AuthController.java` - Fixed lockout timing logic
- `CONFIGURATION.md` - Documented self-registration toggle
- `QUICK_REFERENCE.md` - Added self-registration to quick ref
- `CHANGELOG.md` - This update

---

## [1.0.0] - 2026-02-09

### 🎉 Initial Release

Complete membership portal for The Willows RTA with full authentication, member management, and admin controls.

---

## Detailed Feature History

### Authentication & Security

#### [2026-02-09] - Failed Login Lockout System
**Added:**
- Automatic account lockout after 5 failed login attempts
- 15-minute lockout duration with auto-unlock
- Progressive warnings (shows remaining attempts after 3rd failure)
- Admin ability to manually unlock accounts
- Failed login counter displayed in member details
- Lock expiration time shown to admins

**Files:**
- Added `failedLoginAttempts` and `accountLockedUntil` fields to User model
- Added lockout methods to UserService
- Updated AuthController with lockout checks
- Updated member-details.html with lockout info and unlock button

**Configuration:**
- Max attempts: 5 (configurable in UserService.java)
- Lockout duration: 15 minutes (configurable)
- Auto-unlock on expiration

---

#### [2026-02-09] - Admin Account Management
**Added:**
- Admin can reset member passwords (generates secure 12-char password)
- Admin can lock/unlock member accounts
- Password reset forces password change on next login
- Lock status displayed in member details

**Files:**
- Added reset password endpoint to AdminController
- Added toggle lock endpoint to AdminController
- Updated member-details.html with action buttons
- Added account status badges

**Security:**
- Passwords BCrypt encrypted
- Generated passwords use SecureRandom
- Locked accounts cannot login
- Clear error messages for locked accounts

---

#### [2026-02-09] - Forced Password Change on First Login
**Added:**
- Admin-created accounts require password change on first login
- User redirected to change password page immediately after login
- Cannot bypass password change requirement
- Flag automatically cleared after successful password change

**Files:**
- Added `passwordChangeRequired` field to User model
- Updated AuthController to check flag and redirect
- Updated change-password.html with required notice
- Cancel button hidden when password change is required

**User Experience:**
- Warning banner: "Password Change Required"
- Clear instructions shown
- Security tips displayed

---

#### [2026-02-09] - OTP/MFA Toggle
**Added:**
- Property-based toggle for MFA (on/off)
- Dynamic UI based on MFA status
- Direct login when MFA disabled

**Configuration:**
```properties
app.mfa.enabled=true  # Enable two-factor authentication
```

**Files:**
- Added `mfaEnabled` property to AuthController
- Updated login flow to skip OTP when disabled
- Updated login.html with conditional messaging
- Added configuration notes to application.properties

**Notes:**
- Changing MFA setting requires database reset with H2
- Will be fixed with PostgreSQL migration

---

#### [2026-02-08] - Email-Based OTP/MFA Authentication
**Added:**
- Complete two-factor authentication system
- 6-digit OTP codes sent via email
- 10-minute code expiration
- Resend OTP functionality
- One-time code usage enforcement
- Email masking for privacy

**Files:**
- Created OtpCode entity and repository
- Created OtpService for code generation/validation
- Created EmailService for sending emails
- Created AuthController for OTP flow
- Created verify-otp.html template
- Updated SecurityConfig to allow OTP endpoints

**Email Providers Supported:**
- SendGrid (recommended)
- Gmail
- AWS SES
- Any SMTP server

**Security:**
- Codes stored encrypted in database
- Automatic cleanup of expired codes
- Session timeout (10 minutes)
- Old codes invalidated when new one requested

---

#### [2026-02-08] - Email Uniqueness Enforcement
**Added:**
- Database-level unique constraint on email field
- Registration validation for duplicate emails
- Clear error messages

**Files:**
- Updated Member model with unique constraint
- Updated registration form validation
- Added error handling in PublicController

---

#### [2026-02-08] - Database Encryption (AES)
**Added:**
- File-based H2 database with AES encryption
- Encrypted database file storage
- Secure password for database access

**Configuration:**
```properties
spring.datasource.url=jdbc:h2:file:./data/willowsdb;CIPHER=AES
spring.datasource.password=filepassword WillowsRTA2026!Secure
```

**Security Layers:**
1. Database file encryption (AES)
2. User password encryption (BCrypt)
3. Spring Security authentication

**Documentation:**
- Created DATABASE_SECURITY.md
- Added backup procedures
- Added PostgreSQL migration guide

---

#### [2026-02-07] - Spring Security Integration
**Added:**
- Complete authentication and authorization
- Role-based access control (ADMIN, MEMBER)
- Password encryption (BCrypt)
- Session management
- CSRF protection

**Roles:**
- `ROLE_ADMIN` - Full access to admin panel and all features
- `ROLE_MEMBER` - Access to member dashboard and directory

**Security Features:**
- Passwords never stored in plain text
- Session-based authentication
- Automatic logout on browser close
- Protected routes

---

### Member Management

#### [2026-02-09] - Password Reset Functionality
**Added:**
- Members can change their own password
- Requires current password verification
- Password confirmation matching
- Minimum 8 character requirement
- Client and server-side validation

**Files:**
- Created PasswordResetController
- Created change-password.html template
- Added updatePassword method to UserService
- Added link in member dashboard

**Security:**
- Current password must be verified
- New password must be 8+ characters
- Passwords must match
- BCrypt encryption

---

#### [2026-02-09] - Members Directory
**Added:**
- Public directory for active members
- Limited PII exposure (privacy-focused)
- Clean table layout
- Mobile responsive

**Displayed Information:**
- Full name
- Flat/unit number
- Email (with mailto link)
- Leaseholder status
- Membership status

**NOT Displayed (Privacy):**
- Phone numbers
- Full addresses
- Registration dates
- Signatures

**Files:**
- Created directory.html template
- Added directory endpoint to MemberController
- Added link in member dashboard
- Added CSS styling

---

#### [2026-02-08] - Dual User Registration System
**Added:**
- Two-path registration system
- Self-service registration with account creation
- Admin-assisted registration without account
- Admin can create accounts later for registered members

**Path 1: Self-Service**
- Member creates account during registration
- Sets own password (min 8 chars)
- Can login immediately
- Marked as "SELF_REGISTRATION"

**Path 2: Admin-Created**
- Member registers without account
- Admin creates account later
- Auto-generated secure password
- Marked as "ADMIN_CREATED"

**Files:**
- Updated Member model with account tracking fields
- Updated register.html with account creation options
- Updated PublicController with dual registration logic
- Updated AdminController with account creation
- Added members-no-accounts.html template

**Admin Features:**
- "Pending Login Setup" count on dashboard
- List of members without accounts
- One-click account creation
- Generated credentials displayed once

---

#### [2026-02-07] - Member Registration System
**Added:**
- Public registration form
- Data validation (email, phone, etc.)
- Privacy consent checkbox
- Digital signature capture
- Default ACTIVE status

**Required Fields:**
- Full name
- Email (unique, validated)
- Address
- Flat/unit number
- Phone number
- Leaseholder status
- Privacy consent

**Optional Fields:**
- Preferred communication method

**Files:**
- Created Member entity
- Created registration form
- Created PublicController
- Added validation

---

#### [2026-02-07] - Admin Member Management
**Added:**
- View all members
- Member details page
- Edit member information
- Delete members
- Filter by status (Active/Suspended/Terminated)
- Member statistics on dashboard

**Files:**
- Created AdminController
- Created member management templates
- Added member count statistics
- Added status badges

**Statistics Tracked:**
- Total members
- Active members
- Pending login setup
- Total users

---

### User Interface

#### [2026-02-09] - Header Spacing Fix
**Added:**
- Improved header layout to prevent overlap
- Responsive design for mobile devices
- Better spacing for user info

**Changes:**
- Reduced heading sizes (H1: 2.5em → 2.2em)
- Added padding for user info area
- Added responsive breakpoints
- Mobile-friendly layout

**Files:**
- Updated style.css with new header CSS
- Added media queries for mobile

---

#### [2026-02-09] - Constitution Page Navigation
**Fixed:**
- Constitution back button now uses browser history
- No longer logs user out
- Returns to previous page

**Before:**
```html
<a href="/">← Back to Home</a>  <!-- Logged user out -->
```

**After:**
```html
<button onclick="window.history.back()">← Back</button>
```

---

#### [2026-02-07] - Dashboard Interfaces
**Added:**
- Admin dashboard with statistics
- Member dashboard with action cards
- Navigation menus
- Responsive design

**Admin Dashboard:**
- Member statistics
- Quick actions
- Navigation to all admin features

**Member Dashboard:**
- Profile access
- Members directory
- Constitution link
- Change password link

---

#### [2026-02-07] - Constitution Page
**Added:**
- Full constitution document
- Print/download functionality
- Clean, readable layout
- Public access

**Features:**
- 8 sections covering all aspects
- Printable format
- Back navigation

---

### Documentation

#### [2026-02-09] - Comprehensive Documentation
**Added:**
- CONFIGURATION.md - All configurable settings
- QUICK_REFERENCE.md - Fast lookup guide
- CHANGELOG.md - This file!

**CONFIGURATION.md includes:**
- Email configuration (all providers)
- MFA settings
- Security settings (lockout, passwords, OTP)
- Database configuration
- UI customization
- Server settings
- Environment-specific setups
- Configuration checklists

**QUICK_REFERENCE.md includes:**
- Top 8 most common settings
- File locations with line numbers
- Environment presets
- Post-change instructions

---

#### [2026-02-08] - TROUBLESHOOTING.md
**Added:**
- Forced password change documentation
- MFA toggle issues and solutions
- Constitution navigation fix
- Database reset procedures
- Common error solutions

---

#### [2026-02-08] - EMAIL_SETUP.md
**Added:**
- Complete email configuration guide
- Provider-specific instructions (Gmail, SendGrid, AWS SES)
- Development mode (console output)
- Production setup
- Railway deployment instructions
- Troubleshooting

---

#### [2026-02-08] - DATABASE_SECURITY.md
**Added:**
- Database encryption guide
- Backup procedures
- PostgreSQL migration planning
- Security best practices

---

#### [2026-02-07] - Initial Documentation
**Added:**
- README.md - Project overview
- QUICKSTART.md - Setup guide
- .gitignore - Excluded files

---

### Technical Infrastructure

#### [2026-02-08] - Circular Dependency Fix
**Fixed:**
- Resolved SecurityConfig → UserService circular dependency
- Removed AuthenticationManager bean
- Added @Lazy annotation to DataInitializer

**Impact:**
- Application starts successfully
- No runtime errors
- Proper bean initialization

---

#### [2026-02-07] - Initial Project Setup
**Created:**
- Spring Boot 3.2.1 project
- Maven build configuration
- H2 database integration
- Spring Security setup
- Thymeleaf templates
- Static resources (CSS, JS)

**Dependencies:**
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Validation
- Spring Boot Starter Mail
- H2 Database
- Apache POI (for future document handling)

---

## Security Updates Summary

### Authentication Layers:
1. ✅ Username/password (BCrypt encrypted)
2. ✅ OTP/MFA (email-based, optional)
3. ✅ Failed login lockout (5 attempts, 15 min)
4. ✅ Session management
5. ✅ Role-based authorization (Admin/Member)
6. ✅ CSRF protection

### Data Protection:
1. ✅ Database encryption (AES)
2. ✅ Password encryption (BCrypt)
3. ✅ Email uniqueness
4. ✅ Secure password generation (12 chars, random)
5. ✅ OTP codes (6 digits, 10 min expiration)

### Admin Controls:
1. ✅ Reset member passwords
2. ✅ Lock/unlock accounts
3. ✅ Clear failed login attempts
4. ✅ Create user accounts for members
5. ✅ View security status
6. ✅ Change member roles (Admin/Member)
7. ✅ System admin protection (cannot delete original admin)

### Access Control:
1. ✅ Role-based dashboards (Admin vs Member)
2. ✅ Protected admin routes
3. ✅ System administrator privileges
4. ✅ Role change authorization

---

## Breaking Changes

### None Yet
This is the initial 1.0.0 release. Future breaking changes will be documented here.

### Planned Breaking Changes:
- **PostgreSQL Migration**: Will require database migration when implemented
- **Password Complexity**: May add stricter password requirements

---

## Known Issues

### H2 Database Limitations:
1. **MFA Toggle**: Changing `app.mfa.enabled` requires database reset
2. **Read-Only Errors**: Occasional file corruption requires database deletion
3. **Single Process**: Cannot handle concurrent access well

**Solution**: PostgreSQL migration (planned)

### Email Configuration:
1. **No Email Setup**: OTP codes print to console (intended for development)

**Solution**: Configure email provider (see EMAIL_SETUP.md)

---

## Migration Notes

### From No Database → 1.0.0:
1. Application creates database automatically
2. Default admin account: `admin` / `admin123`
3. Database file: `./data/willowsdb.mv.db`
4. Encrypted with password from application.properties

### Future: H2 → PostgreSQL:
- Will require data export/import
- Will fix MFA toggle issue
- Will improve reliability
- Migration guide to be provided

---

## Configuration Changes by Version

### 1.0.2 (2026-02-09)
**Added Database Fields:**
- `users.system_admin` (boolean) - Marks original admin account as protected

**Added Transient Fields:**
- `Member.userRole` (String) - Display user role in admin views

**No configuration properties added**
**No breaking changes** - fully backward compatible

---

### 1.0.1 (2026-02-09)
**Added Properties:**
```properties
# Self-Registration Control
app.self-registration.enabled=true
```

**Added Database Fields:**
- `Member` transient fields: `userEnabled`, `userAccountLocked`, `userFailedAttempts` (display only, not stored)

**No breaking changes** - fully backward compatible

---

### 1.0.0 (2026-02-09)
**Added Properties:**
```properties
# Email
spring.mail.from=noreply@willowsrta.org
app.email.enabled=false

# MFA
app.mfa.enabled=true
```

**Added Database Fields:**
- `users.failed_login_attempts` (int)
- `users.account_locked_until` (datetime)
- `users.password_change_required` (boolean)
- `members.has_user_account` (boolean)
- `members.account_creation_method` (varchar)
- `otp_codes` table (complete)

---

## Contributors

- Development: Claude (Anthropic)
- Product Design: The Willows RTA Committee
- Testing: The Willows RTA Community

---

## Roadmap

### Version 1.1 (Planned)
- [ ] PostgreSQL migration
- [ ] Admin email notifications
- [ ] Enhanced password requirements
- [ ] Member profile editing
- [ ] Bulk member import (CSV)

### Version 1.2 (Planned)
- [ ] Document library
- [ ] Meeting minutes management
- [ ] Member photo uploads
- [ ] Email templates customization

### Version 2.0 (Future)
- [ ] SMS OTP option
- [ ] Member voting system
- [ ] Event management
- [ ] Financial tracking
- [ ] Mobile app

---

## License

Proprietary - The Willows RTA

---

**For detailed configuration options, see CONFIGURATION.md**  
**For quick reference, see QUICK_REFERENCE.md**  
**For troubleshooting, see TROUBLESHOOTING.md**

---

Last Updated: 2026-02-09
