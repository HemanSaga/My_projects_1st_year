package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;
import com.inventory.util.PasswordUtil;
import com.inventory.util.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * AuthenticationService - Handles user authentication logic
 */
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate user with username and password
     * 
     * @param username The username
     * @param password The plain text password
     * @return The authenticated User object, or null if authentication fails
     */
    public User login(String username, String password) {
        try {
            logger.info("Login attempt for user: {}", username);

            // Validate input
            if (username == null || username.trim().isEmpty()) {
                logger.warn("Login failed: username is empty");
                return null;
            }

            if (password == null || password.isEmpty()) {
                logger.warn("Login failed: password is empty");
                return null;
            }

            // Find user by username
            User user = userDAO.findByUsername(username.trim());

            if (user == null) {
                logger.warn("Login failed: user not found - {}", username);
                return null;
            }

            // Check if user is active
            if (!user.isActive()) {
                logger.warn("Login failed: user is inactive - {}", username);
                return null;
            }

            // Verify password
            boolean passwordMatch = PasswordUtil.verifyPassword(password, user.getPasswordHash());

            if (!passwordMatch) {
                logger.warn("Login failed: incorrect password for user - {}", username);
                return null;
            }

            // Update last login time
            userDAO.updateLastLogin(user.getUserId());

            // Set user in session
            Session.getInstance().setCurrentUser(user);

            logger.info("Login successful for user: {} (Role: {})", username, user.getRole());
            return user;

        } catch (SQLException e) {
            logger.error("Database error during login", e);
            return null;
        }
    }

    /**
     * Logout the current user
     */
    public void logout() {
        Session session = Session.getInstance();
        String username = session.getCurrentUsername();
        session.clearSession();
        logger.info("User logged out: {}", username);
    }

    /**
     * Check if a user is currently logged in
     */
    public boolean isLoggedIn() {
        return Session.getInstance().isLoggedIn();
    }

    /**
     * Get the currently logged-in user
     */
    public User getCurrentUser() {
        return Session.getInstance().getCurrentUser();
    }

    /**
     * Change password for a user
     * 
     * @param userId      The user ID
     * @param oldPassword The old password (for verification)
     * @param newPassword The new password
     * @return true if password was changed successfully
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try {
            // Get user
            User user = userDAO.getById(userId);
            if (user == null) {
                logger.warn("Change password failed: user not found - {}", userId);
                return false;
            }

            // Verify old password
            if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                logger.warn("Change password failed: incorrect old password - {}", userId);
                return false;
            }

            // Hash new password
            String newPasswordHash = PasswordUtil.hashPassword(newPassword);

            // Update password
            boolean success = userDAO.updatePassword(userId, newPasswordHash);

            if (success) {
                logger.info("Password changed successfully for user: {}", user.getUsername());
            }

            return success;

        } catch (SQLException e) {
            logger.error("Database error during password change", e);
            return false;
        }
    }
}