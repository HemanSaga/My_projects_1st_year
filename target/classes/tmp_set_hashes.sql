UPDATE users SET password_hash = '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba' WHERE username = 'admin';
UPDATE users SET password_hash = '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba' WHERE username = 'manager';
UPDATE users SET password_hash = '$2a$10$f6FRPLMzsCsuRFq49VcXFu4yOSlb.Cf2RP6js5TAwETXNgqgcNGba' WHERE username = 'staff';
SELECT username, LENGTH(password_hash) as hash_len, LEFT(password_hash,7) as hash_prefix FROM users;