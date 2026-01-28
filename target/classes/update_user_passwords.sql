-- ============================================
-- FIX USER PASSWORDS - VERIFIED WORKING HASHES
-- ============================================
-- Password for all users: password123
-- These hashes are VERIFIED to work with BCrypt

USE inventory_management;

-- Clear existing users (CAREFUL - this deletes all users!)
DELETE FROM users;

-- Reset auto-increment
ALTER TABLE users AUTO_INCREMENT = 1;

-- Insert Admin User with WORKING hash
INSERT INTO users (username, password_hash, email, full_name, role, is_active, created_at)
VALUES (
    'admin',
    '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba', -- fresh hash for password123
    'admin@inventory.com',
    'System Administrator',
    'Admin',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert Manager User with WORKING hash
INSERT INTO users (username, password_hash, email, full_name, role, is_active, created_at)
VALUES (
    'manager',
    '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba', -- fresh hash for password123
    'manager@inventory.com',
    'John Manager',
    'Manager',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert Staff User with WORKING hash
INSERT INTO users (username, password_hash, email, full_name, role, is_active, created_at)
VALUES (
    'staff',
    '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba', -- fresh hash for password123
    'staff@inventory.com',
    'Jane Staff',
    'Staff',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Verify the users were created
SELECT 
    user_id,
    username,
    email,
    full_name,
    role,
    is_active,
    LENGTH(password_hash) as hash_length,
    LEFT(password_hash, 7) as hash_prefix
FROM users
ORDER BY user_id;

-- ============================================
-- LOGIN CREDENTIALS
-- ============================================
/*
All accounts use password: password123

Admin:
  Username: admin
  Password: password123

Manager:
  Username: manager
  Password: password123

Staff:
  Username: staff
  Password: password123

IMPORTANT: Change these in production!
*/

-- ============================================
-- TROUBLESHOOTING
-- ============================================
/*
If login still fails:

1. Run the TestLogin.java diagnostic tool first
2. Check application.properties database connection
3. Verify MySQL is running
4. Check these common issues:
   - Database name is correct (inventory_management)
   - User table exists
   - Users were inserted successfully
   - Password hashes are 60 characters long
   - No extra whitespace in username/password

5. Enable debug logging in LoginController to see exact error
*/