package com.inventory.util;

public class BcryptTest {
    public static void main(String[] args) {
        String hash = "$2a$10$eQE6KjGGKxLLy3cLPKGOzO8rYJqZd4nFDVLLYNXQ8eX.Hd0gIZ8yS";
        String password = "password123";
        boolean matches = PasswordUtil.verifyPassword(password, hash);
        System.out.println("Hash length: " + (hash != null ? hash.length() : 0));
        System.out.println("Password matches: " + matches);

        // Also test a freshly generated hash
        String newHash = PasswordUtil.hashPassword(password);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash length: " + newHash.length());
        System.out.println("Verify new hash: " + PasswordUtil.verifyPassword(password, newHash));
    }
}
