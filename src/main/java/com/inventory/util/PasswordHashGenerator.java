package com.inventory.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordHashGenerator - Utility to generate BCrypt password hashes
 * Run this to generate hashes for your SQL insert statements
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        String password = "password123";

        System.out.println("===========================================");
        System.out.println("BCrypt Password Hash Generator");
        System.out.println("===========================================");
        System.out.println("Password to hash: " + password);
        System.out.println();

        // Generate multiple hashes (each will be unique due to salt)
        System.out.println("Generated Hashes (use any one):");
        System.out.println("-------------------------------------------");

        for (int i = 1; i <= 5; i++) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            System.out.println(i + ". " + hash);
        }

        System.out.println("-------------------------------------------");
        System.out.println();

        // Test verification
        System.out.println("Testing Verification:");
        String testHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Test Hash: " + testHash);

        boolean matches = BCrypt.checkpw(password, testHash);
        System.out.println("Password matches hash: " + matches);

        System.out.println();
        System.out.println("Copy any hash above into your SQL INSERT statements");
        System.out.println("===========================================");
    }
}