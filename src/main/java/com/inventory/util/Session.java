package com.inventory.util;

import com.inventory.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session - Manages the currently logged-in user session
 * Singleton pattern to ensure only one session exists
 */
public class Session {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);
    private static Session instance;

    private User currentUser;

    private Session() {
        // Private constructor for singleton
    }

    /**
     * Get singleton instance
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            logger.info("User logged in: {} ({})", user.getUsername(), user.getRole());
        }
    }

    /**
     * Get the current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get the username of the current user
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    /**
     * Get the full name of the current user
     */
    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }

    /**
     * Get the role of the current user
     */
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : "GUEST";
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Check if current user is manager
     */
    public boolean isManager() {
        return currentUser != null && currentUser.isManager();
    }

    /**
     * Check if current user is staff
     */
    public boolean isStaff() {
        return currentUser != null && currentUser.isStaff();
    }

    /**
     * Clear the current session (logout)
     */
    public void clearSession() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
        }
        this.currentUser = null;
    }

    /**
     * Get user ID of current user
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
}