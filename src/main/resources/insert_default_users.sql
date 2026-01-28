-- ============================================
-- Insert Default Users for Testing - FIXED VERSION
-- ============================================
-- All passwords are: "password123"
-- Hashes generated with BCrypt work factor 10

USE inventory_management;

-- Clear existing users (optional - be careful in production!)
-- DELETE FROM users;

-- Insert Admin User
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

-- Insert Manager User  
INSERT INTO users (username, password_hash, email, full_name, role, is_active, created_at)
VALUES (
    'manager',
    '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba',
    'manager@inventory.com',
    'John Manager',
    'Manager',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert Staff User
INSERT INTO users (username, password_hash, email, full_name, role, is_active, created_at)
VALUES (
    'staff',
    '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba',
    'staff@inventory.com',
    'Jane Staff',
    'Staff',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Verify inserted users
SELECT 
    user_id,
    username,
    email,
    full_name,
    role,
    is_active,
    created_at,
    LENGTH(password_hash) as hash_length
FROM users
ORDER BY user_id;

-- ============================================
-- DEFAULT LOGIN CREDENTIALS
-- ============================================
/*
Admin Account:
  Username: admin
  Password: password123
  
Manager Account:
  Username: manager
  Password: password123
  
Staff Account:
  Username: staff
  Password: password123

IMPORTANT: Change these passwords in production!
*/