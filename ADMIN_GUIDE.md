# Admin Guide - The Willows RTA Portal

Complete guide for administrators managing the membership portal.

---

## 🔐 Admin Access

### Default Admin Account
- **Username:** `admin`
- **Password:** `admin123`
- ⚠️ **CHANGE THIS IMMEDIATELY ON FIRST LOGIN**

### Admin Dashboard
After login, admins are redirected to `/admin/dashboard`

**Dashboard Shows:**
- Total Members
- Active Members  
- Members Pending Login Setup
- Quick actions to member management

---

## 👥 Member Management

### Add New Member (NEW v1.0.3)

**Path:** Admin Dashboard → "➕ Add New Member" OR `/admin/members/add`

**Purpose:** Manually add members from paper forms, email submissions, or in-person registrations

**Information Required:**
- **Personal:** Full name, email, phone
- **Address:** Full address, flat/unit number
- **Membership:** Leaseholder status, membership status, preferred communication
- **Consent:** Digital signature, constitution agreement

**Account Creation Options:**

1. **Auto-Generate Password (Recommended)**
   - System creates secure 12-character password
   - Displayed once after creation
   - Member forced to change on first login

2. **Set Password Manually**
   - Admin sets specific password (min 8 chars)
   - Requires password confirmation
   - Useful for in-person registration

3. **Create Account Later**
   - Just add member details
   - Create login from member details page later
   - Good for bulk data entry

**Process:**
1. Click "Add New Member"
2. Fill in all required fields (marked with *)
3. Choose account creation option
4. Click "Add Member"
5. View generated credentials (if account created)
6. Share credentials securely with member

**Validation:**
- Email must be unique (checks for duplicates)
- All required fields must be filled
- Password must match confirmation (if manual)

**After Creation:**
- Redirected to member details page
- Success message shows credentials (if account created)
- Member appears in members list immediately

---

### View All Members
**Path:** Admin Dashboard → "View All Members" OR `/admin/members`

**Members List Shows:**
- ID, Full Name, Flat/Unit
- Email, Phone
- Leaseholder status
- Has Login account (✓/✗)
- **Role** (Admin/Member) - NEW in v1.0.2
- **Account Status** (Active/Locked/Temp Lock/X fails) - NEW in v1.0.1
- Membership status (Active/Suspended/Terminated)
- Registration date
- Actions (View/Edit/Delete)

**Account Status Indicators:**
- **Active** 🟢 - Account in good standing
- **X fails** ⚠️ - Has failed login attempts (e.g., "3 fails")
- **⏱️ Temp Lock** 🟡 - Temporarily locked due to failed logins
- **🔒 Locked** 🔴 - Manually locked by admin
- **-** ⚪ - No user account

**Role Indicators:**
- **Admin** 🔴 - Administrator role (red badge)
- **Member** 🟢 - Standard member role (green badge)
- **-** ⚪ - No user account

---

## 👤 Member Details

**Path:** Members List → Click "View" on any member

### Information Displayed

**Personal Information:**
- Full name, Email, Phone
- Address, Flat/Unit number
- Leaseholder status
- Membership status
- Preferred communication method

**Login Account Status:**
- Has Login Account (Yes/No)
- Account Status (Active/Locked)
- Account Created By (Self/Admin)
- Login Username (email)
- **Role** (Administrator/Member)
- **System Admin** badge (for protected admin account)
- Password Change Required (Yes/No)
- Failed Login Attempts count
- Account Locked Until (if temp locked)

**Signature:**
- Digital signature

---

## 🔑 Account Management Actions

### 1. Reset Password

**Button:** 🔑 Reset Password

**What It Does:**
- Generates secure 12-character random password
- Forces password change on next login
- Displays new password to admin (one time only)

**Use When:**
- Member forgot password
- Security concern
- Initial setup failed

**Process:**
1. Click "Reset Password"
2. Confirm action
3. Copy displayed password
4. Share securely with member
5. Member must change on next login

---

### 2. Change Role (NEW v1.0.2)

**Buttons:** 
- 👑 Make Admin (if currently Member)
- 👤 Make Member (if currently Admin)

**What It Does:**
- Promotes member to administrator role
- OR demotes admin to member role
- Role change is immediate
- Affects access permissions

**Use When:**
- Need additional administrators
- Removing admin privileges
- Reorganizing committee roles

**Process:**
1. Click "Make Admin" or "Make Member"
2. Confirm role change
3. Role updates immediately
4. User sees new dashboard on next login

**Restrictions:**
- Cannot change role of System Administrator (original admin account)
- Button hidden for system admin
- Only accounts with logins can have roles

**Admin vs Member Permissions:**

| Feature | Admin | Member |
|---------|-------|--------|
| View own profile | ✅ | ✅ |
| View members directory | ✅ | ✅ |
| View all member details | ✅ | ❌ |
| Edit members | ✅ | ❌ |
| Delete members | ✅ | ❌ |
| Create login accounts | ✅ | ❌ |
| Lock/unlock accounts | ✅ | ❌ |
| Reset passwords | ✅ | ❌ |
| Change roles | ✅ | ❌ |
| View statistics | ✅ | ❌ |

---

### 3. Clear Failed Login Attempts

**Button:** 🔓 Clear Failed Attempts

**Shows When:** Member has failed login attempts > 0

**What It Does:**
- Resets failed login counter to 0
- Clears temporary lockout
- Member can login immediately

**Use When:**
- Member locked out due to forgotten password
- Member exceeded attempts legitimately
- You've verified member's identity

**Process:**
1. Click "Clear Failed Attempts"
2. Confirm action
3. Counter reset to 0
4. Lock cleared
5. Member can login

---

### 4. Lock/Unlock Account

**Buttons:**
- 🔒 Lock Account (if currently enabled)
- 🔓 Unlock Account (if currently locked)

**What It Does:**
- Manually prevents/allows login
- Permanent until unlocked (unlike temp lock)
- Member sees "Account locked, contact administrator"

**Use When:**
- Suspicious activity
- Member violation
- Temporary suspension needed
- Security investigation

**Lock Process:**
1. Click "Lock Account"
2. Confirm action
3. Account disabled
4. Member cannot login

**Unlock Process:**
1. Click "Unlock Account"
2. Confirm action
3. Account enabled
4. Member can login

---

## 🆕 Create Login Accounts

### Members Without Accounts

**Path:** Admin Dashboard → "Pending Login Setup" count

**Shows:** Members who registered but don't have login accounts

**Create Account Process:**
1. View member without account
2. Click "Create Login Account"
3. System generates secure password
4. Credentials displayed (one time only)
5. Copy and share securely with member
6. Member must change password on first login

**Auto-Generated:**
- Username: Member's email
- Password: Random 12 characters
- Role: ROLE_MEMBER
- Password change required: Yes

---

## 🔐 System Administrator Protection

### What Is a System Administrator?

The **original admin account** (username: `admin`) is marked as a System Administrator.

**Protection Features:**
- ✅ Cannot be deleted
- ✅ Cannot have role changed to Member
- ✅ Always has admin privileges
- ✅ Flagged with "System Admin" badge

**Why?**
Prevents accidental lockout from the system. At least one admin account always exists.

**Other Admin Accounts:**
- Can be created by promoting members
- Can be deleted if needed
- Can be demoted back to member
- Not protected

---

## 📊 Security Monitoring

### Failed Login Attempts

**View In:**
- Members list: "Account Status" column shows "X fails"
- Member details: "Failed Login Attempts" row

**Automatic Lockout:**
- After 5 failed attempts
- Locked for 15 minutes
- Auto-unlocks after time expires
- Admin can unlock manually anytime

**Progressive Warnings (User Sees):**
```
Attempt 1-2: "Invalid username or password"
Attempt 3:   "...3 attempts remaining before lock"
Attempt 4:   "...2 attempts remaining before lock"
Attempt 5:   "...1 attempt remaining before lock"
Attempt 6+:  "Account locked... try again in 15 minutes"
```

### Account Status Monitoring

**Check Members List:**
- Look for 🔒 Locked
- Look for ⏱️ Temp Lock  
- Look for warning badges (3+ fails)

**Take Action:**
- Investigate suspicious activity
- Contact member if needed
- Clear failed attempts if legitimate
- Lock account if security concern

---

## ✏️ Edit Member Details

**Path:** Member Details → "Edit Member Details"

**Can Edit:**
- Full name
- Email (must remain unique)
- Phone number
- Address
- Flat/Unit number
- Leaseholder status
- Membership status (Active/Suspended/Terminated)
- Preferred communication

**Cannot Edit:**
- Registration date
- Account creation method
- Login credentials (use reset password instead)
- Role (use change role button instead)

---

## 🗑️ Delete Members

**Path:** Member Details → "Delete Member"

**What Gets Deleted:**
1. User account (if exists)
2. Member record
3. All associated data

**Cannot Delete:**
- System Administrator account
- Shows warning: "System administrator account cannot be deleted"

**Confirmation Required:**
"Are you sure you want to delete this member? This action cannot be undone."

**Process:**
1. View member details
2. Click "Delete Member"
3. Confirm deletion
4. Member removed from system

⚠️ **This is permanent!** No undo available.

---

## 🔍 Filtering and Search

### Filter by Status

**Dropdown:** Active / Suspended / Terminated

**Use When:**
- Need to see only active members
- Review suspended accounts
- Audit terminated memberships

### Members Without Accounts

**Link:** "View Members Without Accounts"

**Shows:** All registered members who don't have login credentials

**Common Reasons:**
- Member chose "No" during self-registration
- Self-registration was disabled
- Admin hasn't created account yet

**Action:** Click "Create Login Account" to generate credentials

---

## 🎨 Visual Indicators

### Username Colors (NEW v1.0.2)

**In Header (Top-Right):**
- **Admin users:** Username in RED
- **Member users:** Username in black

**Why?** Instant visual confirmation of your current role.

### Badges

**Role Badges:**
- 🔴 **Admin** - Red background
- 🟢 **Member** - Green background
- 🟡 **System Admin** - Yellow/warning style

**Status Badges:**
- 🟢 **Active** - Green
- 🟡 **Suspended** - Yellow/Orange
- 🔴 **Terminated** - Red
- 🟢 **Yes** (Leaseholder) - Green
- 🟡 **No** (Leaseholder) - Yellow

**Account Status Badges:**
- 🟢 **Active** - Good standing
- 🟡 **X fails** - Warning
- 🟡 **⏱️ Temp Lock** - Temporary lockout
- 🔴 **🔒 Locked** - Admin locked

---

## 📋 Best Practices

### Account Security

1. **Change default admin password immediately**
2. **Don't share admin credentials**
3. **Create separate admin accounts for each administrator**
4. **Use self-registration toggle wisely** (require approval for high security)
5. **Monitor failed login attempts regularly**
6. **Lock accounts if suspicious activity detected**
7. **Clear failed attempts only after verifying member identity**

### Role Management

1. **Don't promote members to admin casually**
2. **Review admin list regularly**
3. **Demote admins who no longer need access**
4. **Keep at least 2 admin accounts** (backup if one locked out)
5. **Never delete the system administrator account** (you can't anyway!)

### Member Management

1. **Verify member details before creating login accounts**
2. **Use secure method to share initial passwords** (not email!)
3. **Reset passwords if you suspect compromise**
4. **Review membership status quarterly**
5. **Update member information promptly**
6. **Delete members only when absolutely necessary**

### Password Security

1. **Force password change on first login** (automatic for admin-created accounts)
2. **Reset passwords if member reports suspicious activity**
3. **Don't reuse passwords from reset**
4. **Encourage members to use strong passwords**
5. **Minimum 8 characters enforced** (consider increasing to 10-12)

---

## 🆘 Common Admin Tasks

### Add Member from Paper Form (NEW v1.0.3)

1. Click "Add New Member" from dashboard
2. Enter member details from form
3. Select "Yes - Create login account"
4. Select "Auto-generate secure password"
5. Click "Add Member"
6. Copy displayed password
7. Contact member with credentials (phone/in-person)

### Add Member from Email

1. Receive member registration email
2. Click "Add New Member"
3. Copy details from email
4. Auto-generate password
5. Reply to email with credentials (securely)

### In-Person Registration

1. Member present with admin
2. Click "Add New Member"
3. Enter details together
4. Select "Set password manually"
5. Member provides password
6. Confirm password
7. Member can login immediately

### Bulk Data Entry

1. Click "Add New Member"
2. Enter member details
3. Select "No - Create account later"
4. Repeat for all members
5. Later: Create accounts in batch from member details pages

---

### New Member Joins

**Self-Registration Enabled:**
1. Member registers online
2. Member creates account (optional)
3. If member created account → They can login immediately
4. If member skipped account → Create login for them
5. Share credentials securely

**Self-Registration Disabled:**
1. Member registers online
2. Review registration in admin panel
3. Create login account
4. Share credentials securely
5. Member logs in and changes password

### Member Forgot Password

1. Go to member details
2. Click "Reset Password"
3. Copy generated password
4. Share securely (phone call, in person, Signal, etc.)
5. Member logs in
6. Forced to change password

### Suspicious Account Activity

1. Check failed login attempts
2. If high (3+), investigate
3. Contact member to verify
4. If unauthorized access suspected:
   - Lock account immediately
   - Reset password
   - Clear failed attempts
   - Unlock account
   - Share new password securely

### Member Locked Out

**Temp Lock (Failed Attempts):**
1. View member details
2. Click "Clear Failed Attempts"
3. Notify member they can login

**Admin Lock:**
1. View member details
2. Click "Unlock Account"
3. Notify member

### Need Additional Admin

1. Member must already have login account
2. Go to member details
3. Click "👑 Make Admin"
4. Confirm
5. Notify user of new privileges

### Remove Admin Privileges

1. Go to admin user's member details
2. Click "👤 Make Member"
3. Confirm
4. Notify user of change

### Member Leaves

1. Change membership status to "Terminated"
2. Lock account (prevents login but preserves data)
3. OR delete member entirely (permanent)

---

## ⚙️ Configuration Changes

See `CONFIGURATION.md` for complete configuration guide.

**Quick Settings:**

**Require Admin Approval for All Accounts:**
```properties
app.self-registration.enabled=false
```

**Allow Members to Create Accounts:**
```properties
app.self-registration.enabled=true
```

**Disable MFA (Development):**
```properties
app.mfa.enabled=false
```

**Enable MFA (Production):**
```properties
app.mfa.enabled=true
```

---

## 📌 Notice Board Management (NEW v1.0.4)

### Overview
The Notice Board allows admins to post announcements, updates, and important information for all members.

### Creating a Notice

**Path:** Notice Board → "+ Create Notice"

1. Click **"+ Create Notice"** button
2. Enter **Title** (e.g., "Annual General Meeting - 15th March")
3. Enter **Content** (multi-line supported)
4. Click **"Post Notice"**

**Tips:**
- Keep titles clear and concise
- Use proper formatting in content
- Important notices should be pinned
- Include dates/times for events

### Editing a Notice

1. Go to Notice Board
2. Find the notice
3. Click **"Edit"** button
4. Update title or content
5. Click **"Update Notice"**

**Note:** Original post date and author are preserved

### Pinning Notices

**Important notices appear at the top!**

1. Find the notice
2. Click **"📌 Pin"** button
3. Notice moves to top with pin badge
4. Click **"Unpin"** to remove pin

**Use For:**
- Urgent announcements
- Upcoming meetings
- Important deadlines
- Emergency notices

### Deleting Notices

1. Find the notice
2. Click **"Delete"** button
3. Confirm deletion

**Warning:** Deletion is permanent!

### Member View

**All logged-in members can:**
- View all notices
- See who posted and when
- Access via "Notice Board" navigation link

**Members cannot:**
- Create notices
- Edit notices
- Delete notices
- Pin/unpin notices

---

## 👁️ Enhanced Member Directory (NEW v1.0.4)

### Admin View
Admins now see **full member information** in the Members Directory:
- Name, Flat/Unit
- Email, Phone
- **Full Address**
- Leaseholder status
- Membership status
- **Login Account Status** (Yes/No)

### Member View
Regular members see **limited information** for privacy:
- Name, Flat/Unit
- Email only
- Leaseholder status
- Membership status

This ensures admins have necessary contact details while protecting member privacy.

---

## 📞 Support

**Documentation Files:**
- `README.md` - Overview and setup
- `CONFIGURATION.md` - All configuration options
- `QUICK_REFERENCE.md` - Fast lookup
- `TROUBLESHOOTING.md` - Common issues
- `CHANGELOG.md` - Version history
- `EMAIL_SETUP.md` - Email configuration
- `DATABASE_SECURITY.md` - Security details

**For Technical Issues:**
See `TROUBLESHOOTING.md`

---

**Version:** 1.0.4  
**Last Updated:** February 2026
