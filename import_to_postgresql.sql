-- PostgreSQL Import Script
-- Converted from H2 export for Willows RTA Portal
-- Export Date: 2026-02-11

-- Note: Run this AFTER starting the Spring Boot app once with PostgreSQL
-- (This ensures tables are created, then we'll insert the data)

-- First, clear any existing data (if reimporting)
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE members CASCADE;
TRUNCATE TABLE otp_codes CASCADE;

-- Reset sequences to start from correct values
ALTER SEQUENCE members_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE otp_codes_id_seq RESTART WITH 1;

-- Insert Members
-- Note: H2's IS_LEASEHOLDER becomes leaseholder in PostgreSQL (column name mapping)
INSERT INTO members (id, account_creation_method, address, consent_given, email, flat_number, full_name, has_user_account, leaseholder, membership_status, phone_number, preferred_communication, registration_date, signature_data) VALUES
(1, NULL, 'sss', TRUE, 'dork@gmail.com', '7 Windings house', 'Goig', FALSE, FALSE, 'ACTIVE', '55555', 'PHONE', '2026-02-11 16:07:22.404554', 'Goig'),
(2, 'ADMIN_CREATED', 'afa', TRUE, 'dale_macdonald@hotmail.com', '9 Windings house', 'Dale Macdonald', TRUE, TRUE, 'ACTIVE', '07590999046', 'EMAIL', '2026-02-11 16:08:14.034095', 'DTMacdonald'),
(3, NULL, 'ssadf', TRUE, 'bob@a.com', '4 Clustoid', 'bob', FALSE, FALSE, 'ACTIVE', '65478647', 'PHONE', '2026-02-11 16:08:38.762626', 'Bob'),
(4, 'ADMIN_CREATED', 'zxcf', TRUE, 'dork2@gmail.com', '7 Windings house', 'Goig', TRUE, TRUE, 'ACTIVE', '55555', 'POST', '2026-02-11 16:09:24.367795', 'Goig');

-- Insert Users
INSERT INTO users (id, account_locked_until, enabled, failed_login_attempts, last_login, password, password_change_required, role, system_admin, username, member_id) VALUES
(1, NULL, TRUE, 0, '2026-02-11 16:09:35.405726', '$2a$10$fO0lkRc8uEGmHWtHSkxlZeHyV.h85MU.ilfga4KPIYd4d0pI4TgNS', FALSE, 'ROLE_ADMIN', TRUE, 'admin', NULL),
(2, NULL, TRUE, 0, NULL, '$2a$10$pQyNbwOhsAz6o/IqyLyynuBtyXq4Ys.ESd0jHI6J9dQrtYqD4AD/G', TRUE, 'ROLE_MEMBER', FALSE, 'dale_macdonald@hotmail.com', 2),
(3, NULL, TRUE, 0, NULL, '$2a$10$nsQQiXzRMwWvbduZMdh03OnmorteVWELRBAt0SXyZbcaEckyUgw9u', TRUE, 'ROLE_ADMIN', FALSE, 'dork2@gmail.com', 4);

-- Insert OTP Codes (optional - these expire in 10 minutes anyway)
-- Skipping OTP codes as they're already expired

-- Update sequences to continue from where we left off
SELECT setval('members_id_seq', (SELECT MAX(id) FROM members));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('otp_codes_id_seq', 1); -- Start fresh for OTP codes

-- Verify import
SELECT 'Members imported: ' || COUNT(*) FROM members;
SELECT 'Users imported: ' || COUNT(*) FROM users;

-- Show the data
SELECT id, full_name, email, has_user_account FROM members ORDER BY id;
SELECT id, username, role, system_admin FROM users ORDER BY id;

-- Verify relationships
SELECT m.full_name, u.username, u.role
FROM members m
LEFT JOIN users u ON u.member_id = m.id
WHERE m.has_user_account = true;

-- Success message
SELECT 'Import completed successfully!' AS status;
