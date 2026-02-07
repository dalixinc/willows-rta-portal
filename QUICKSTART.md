# 🚀 Quick Start Guide - The Willows RTA Portal

## What You Have

A complete membership portal for your Residents' Association with:
- Member registration matching your form
- Your actual constitution integrated
- Admin dashboard for managing members
- Secure login system
- Professional, responsive design

## Getting Started in 3 Steps

### Step 1: Make Sure You Have Java 17+

Open terminal/command prompt and type:
```
java -version
```

Should show version 17 or higher. If not, download from https://adoptium.net/

### Step 2: Run the Application

Navigate to the project folder:
```bash
cd willows-rta-portal
```

Run it:
```bash
mvn spring-boot:run
```

Wait for the message: "Started WillowsRtaPortalApplication"

### Step 3: Open Your Browser

Go to:
```
http://localhost:8082
```

## First Time Login

**⚠️ IMPORTANT - Default Admin Credentials:**
```
Username: admin
Password: admin123
```

**YOU MUST CHANGE THIS PASSWORD IMMEDIATELY!**

## What to Do First

### As Admin:

1. **Login** at http://localhost:8082/login
2. **Explore the dashboard** - see membership statistics
3. **View members** - check the members section
4. **Test registration** - try registering a test member
5. **View constitution** - make sure it displays correctly
6. **Change admin password** (feature to add)

### Test the Registration Flow:

1. Go to home page
2. Click "Register Now"
3. Fill in a test member:
   - Name: Test Member
   - Flat: 1A
   - Email: test@example.com
   - Phone: 01234567890
   - Address: Test Address
   - Select "Yes" for leaseholder
   - Choose communication preference
   - Check consent box
   - Sign your name
4. Submit
5. Login as admin and see the new member in the list!

## URL Quick Reference

- **Home Page:** http://localhost:8082
- **Register:** http://localhost:8082/register
- **Login:** http://localhost:8082/login
- **Constitution:** http://localhost:8082/constitution
- **Admin Dashboard:** http://localhost:8082/admin/dashboard (after login)
- **H2 Database Console:** http://localhost:8082/h2-console

## Port Already in Use?

If port 8082 is taken, edit:
`src/main/resources/application.properties`

Change this line:
```properties
server.port=8082
```

To any free port like 8083, 8084, etc.

## What's Included

✅ Member registration form (matches your actual form)
✅ Your constitution (viewable and printable)
✅ Admin dashboard with statistics
✅ Member management (view, edit, delete)
✅ Membership status tracking (Active/Suspended/Terminated)
✅ Secure authentication with Spring Security
✅ Professional green-themed design
✅ Mobile-responsive layout
✅ Database storage (H2 - easily upgradeable)

## Database Info

**Type:** File-based H2 with AES encryption  
**Location:** `data/willowsdb.mv.db` (created automatically)  
**Persistence:** ✅ Your data survives app restarts!  
**Security:** Database file is encrypted

**Important:**
- The `data/` folder will be created when you first run the app
- Your member registrations are saved permanently (until you delete the folder)
- Backup the `data/` folder regularly
- See `DATABASE_SECURITY.md` for security best practices

## User Roles

### ADMIN
- Access to admin dashboard
- View all members
- Edit member details
- Change member status
- Delete members
- View constitution

### MEMBER (Future Feature)
- Personal dashboard
- View own profile
- View constitution
- (Currently registration creates member record, but committee creates login)

## Common Issues

**"Port 8082 already in use"**
→ Change port in application.properties

**"Maven not found"**
→ Install Maven or use an IDE (IntelliJ/Eclipse/VS Code)

**Can't see newly registered member**
→ Login as admin and go to "Members" section

**Forgot admin password**
→ Restart app (default admin recreated on startup)

## Customization

Want to make it your own?

### Change Colors:
Edit `src/main/resources/static/css/style.css`
- Current theme: Green (#2E7D32)
- Change to your preferred color

### Change Logo/Name:
Edit templates to update "The Willows RTA" to your preferred branding

### Add Features:
- Email notifications
- Document uploads
- Meeting scheduler
- News/announcements
- Member forums

## Next Steps

1. ✅ Get it running
2. ⚙️ Customize colors and branding if desired
3. 🧪 Test all features thoroughly
4. 👥 Import existing members (if any)
5. 🔒 Set up proper admin password
6. 📧 Consider adding email notifications
7. 🚀 Deploy to production server when ready

## Production Deployment

When ready to go live:

1. Switch to real database (PostgreSQL recommended)
2. Get a domain name (e.g., willowsrta.org)
3. Get SSL certificate for HTTPS
4. Deploy to hosting service:
   - Heroku (easiest)
   - AWS
   - DigitalOcean
   - Your own server

5. Set up regular backups
6. Create proper admin accounts
7. Delete default admin user

## Support

For detailed information, see the full README.md file.

---

**Enjoy your new membership portal!** 🏘️

Any questions? The code is well-commented and organized to help you understand and modify it.
