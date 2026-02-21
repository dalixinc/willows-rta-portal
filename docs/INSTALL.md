# 📊 Analytics Feature - Clean Installation Guide

## 🎯 What This Package Contains

A complete membership analytics system with vertical bar charts showing take-up per block.

## 📦 Package Contents

```
analytics-clean/
├── java/
│   ├── model/
│   │   ├── Block.java              # Block entity
│   │   └── BlockStats.java         # Analytics DTO
│   ├── repository/
│   │   └── BlockRepository.java    # Database access
│   ├── service/
│   │   └── BlockService.java       # Business logic
│   └── controller/
│       └── AdminController.java    # COMPLETE REPLACEMENT
├── templates/admin/
│   ├── blocks.html                 # Block configuration page
│   ├── block-form.html             # Add/Edit block form
│   └── analytics.html              # Analytics dashboard
├── sql/
│   └── blocks-schema.sql           # Database setup
└── docs/
    ├── INSTALL.md                  # This file
    ├── MEMBERREPOSITORY.md         # Required change
    └── NAVIGATION.md               # Nav updates needed
```

## ⚠️ IMPORTANT: Clean Installation

This package contains a COMPLETE AdminController.java that replaces your existing one.

It has been customized to work with YOUR UserService methods:
- Uses `createUser()` instead of `createUserAccount()`
- Uses `unlockAccount(username)` instead of `lockAccount(id)`
- Uses `updatePassword(id, password)` correctly
- Removes methods you don't have (promoteToAdmin, demoteToAdmin, etc.)

## 🚀 Installation Steps

### Step 1: Database (5 min)

**Run the SQL in Railway PostgreSQL:**

```sql
-- Copy contents of sql/blocks-schema.sql
-- Paste into Railway Query interface
-- Execute
```

**Verify:**
```sql
SELECT * FROM blocks;
-- Should show 4 sample blocks
```

### Step 2: Copy Java Files (10 min)

**Copy in this exact order:**

```bash
# 1. Entities
cp java/model/Block.java src/main/java/com/willows/rta/model/
cp java/model/BlockStats.java src/main/java/com/willows/rta/model/

# 2. Repository
cp java/repository/BlockRepository.java src/main/java/com/willows/rta/repository/

# 3. Service
cp java/service/BlockService.java src/main/java/com/willows/rta/service/

# 4. Controller (REPLACES existing AdminController!)
cp java/controller/AdminController.java src/main/java/com/willows/rta/controller/
```

### Step 3: Update MemberRepository (2 min)

**Add this method to `MemberRepository.java`:**

```java
/**
 * Count active members by address containing block name (case-insensitive)
 */
int countByMembershipStatusAndAddressContainingIgnoreCase(String membershipStatus, String addressFragment);
```

### Step 4: Copy HTML Templates (5 min)

```bash
cp templates/admin/blocks.html src/main/resources/templates/admin/
cp templates/admin/block-form.html src/main/resources/templates/admin/
cp templates/admin/analytics.html src/main/resources/templates/admin/
```

### Step 5: Update Navigation (10 min)

**Add these links to EVERY admin template's navigation:**

Files to update:
- admin/dashboard.html
- admin/members.html
- admin/member-details.html
- admin/members-no-accounts.html
- admin/add-member.html
- admin/edit-member.html

**Find:**
```html
<nav class="admin-nav">
    <a href="/admin/dashboard">Dashboard</a>
    <a href="/admin/members">Members</a>
    <a href="/member/directory">Members Directory</a>
    <a href="/notices">Notice Board</a>
    <a href="/chat">Chat</a>
    <a href="/constitution">Constitution</a>
</nav>
```

**Replace with:**
```html
<nav class="admin-nav">
    <a href="/admin/dashboard">Dashboard</a>
    <a href="/admin/members">Members</a>
    <a href="/member/directory">Members Directory</a>
    <a href="/notices">Notice Board</a>
    <a href="/chat">Chat</a>
    <a href="/admin/analytics">Analytics</a>        <!-- NEW -->
    <a href="/admin/blocks">Block Config</a>       <!-- NEW -->
    <a href="/constitution">Constitution</a>
</nav>
```

### Step 6: Build & Test (5 min)

```bash
mvn clean package
mvn spring-boot:run
```

**Test URLs:**
```
http://localhost:8080/admin/blocks
http://localhost:8080/admin/analytics
```

## ✅ Success Checklist

- [ ] SQL executed (blocks table exists with 4 sample rows)
- [ ] Block.java copied to model/
- [ ] BlockStats.java copied to model/
- [ ] BlockRepository.java copied to repository/
- [ ] BlockService.java copied to service/
- [ ] AdminController.java REPLACED in controller/
- [ ] MemberRepository method added
- [ ] blocks.html copied to templates/admin/
- [ ] block-form.html copied to templates/admin/
- [ ] analytics.html copied to templates/admin/
- [ ] Navigation updated in all admin templates
- [ ] mvn clean package succeeds
- [ ] Can access /admin/blocks
- [ ] Can access /admin/analytics
- [ ] Chart displays with data

## 🎨 What You'll See

**Block Configuration Page:**
- List of all blocks
- Add/Edit/Delete buttons
- Configure: Name, Short Name, Total Flats, Display Order

**Analytics Dashboard:**
- Overall stats cards (Total Members, Total Flats, %, Blocks)
- Beautiful Chart.js vertical bar chart
- Green bars (current members) vs Gray bars (total flats)
- Interactive hover tooltips
- Data table with color-coded status

## 🐛 Troubleshooting

**Build fails on Block.java:**
→ Check file is in correct package: `com.willows.rta.model`

**Build fails on AdminController:**
→ Make sure you REPLACED the file, not merged it

**Can't access /admin/blocks (404):**
→ AdminController methods not loaded. Check build logs.

**Analytics shows no data:**
→ Check blocks table has data
→ Check member addresses contain block names

**Chart doesn't display:**
→ Check browser console for errors
→ Verify Chart.js CDN is loading

## 💡 How Member Counting Works

**Members are counted by address matching:**

```
Member address: "12A Windings House, The Willows"
Block name: "Windings House"
Match: YES → Count this member
```

**Matching is:**
- Case-insensitive
- Substring match (contains)
- Only ACTIVE members

## 🎯 Sample Data

The SQL creates 4 sample blocks:
- Windings House (32 flats)
- Field House (30 flats)
- Bluster House (28 flats)
- Ashby House (25 flats)

**Edit or delete these after installation to match your actual blocks!**

## 🚀 After Installation

1. Go to Block Config
2. Delete sample blocks
3. Add your real blocks with correct totals
4. Go to Analytics
5. See beautiful charts! 📊✨

---

**Built with ❤️ by Dale & Primus**
