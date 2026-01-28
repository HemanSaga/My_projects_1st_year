package com.inventory.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil - Utility for password hashing and verification using BCrypt
 */
public class PasswordUtil {

    // BCrypt work factor (10 is recommended, higher = more secure but slower)
    private static final int WORK_FACTOR = 10;

    /**
     * Hash a plain text password using BCrypt
     * 
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verify a plain text password against a hashed password
     * 
     * @param plainPassword  The plain text password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Invalid hash format or other BCrypt error
            return false;
        }
    }

    /**
     * Check if a password needs rehashing (if work factor has changed)
     * 
     * @param hashedPassword The hashed password
     * @return true if password needs rehashing
     */
    public static boolean needsRehash(String hashedPassword) {
        // BCrypt format: $2a$10$...
        // Extract work factor from hash
        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length >= 3) {
                int workFactor = Integer.parseInt(parts[2]);
                return workFactor != WORK_FACTOR;
            }
        } catch (Exception e) {
            // If can't parse, assume needs rehash
            return true;
        }
        return false;
    }
}