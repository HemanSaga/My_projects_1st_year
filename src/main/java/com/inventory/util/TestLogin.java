package com.inventory.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Simple diagnostic tool to verify default user passwords in the database.
 * Run as: java -cp <classpath> com.inventory.util.TestLogin
 */
public class TestLogin {
    public static void main(String[] args) {
        String[] users = {"admin", "manager", "staff"};

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT username, password_hash, is_active FROM users WHERE username = ?");
            for (String u : users) {
                stmt.setString(1, u);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username");
                        String hash = rs.getString("password_hash");
                        boolean active = rs.getBoolean("is_active");
                        boolean ok = PasswordUtil.verifyPassword("password123", hash);
                        System.out.printf("User: %s, active=%s, hashLen=%d, password123Matches=%s\n", username, active, hash != null ? hash.length() : 0, ok);
                    } else {
                        System.out.printf("User %s not found in DB\n", u);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
