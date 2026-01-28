package com.inventory.controller;

import com.inventory.model.User;
import com.inventory.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * MainController - Main application window with user session management
 */
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label lastLoginLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private ScrollPane centerScroll;

    // Menu items
    @FXML
    private MenuItem menuItemProfile;
    @FXML
    private MenuItem menuItemLogout;
    @FXML
    private MenuItem menuItemAbout;

    // Navigation buttons
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnProducts;
    @FXML
    private Button btnCategories;
    @FXML
    private Button btnSuppliers;
    @FXML
    private Button btnOrders;
    @FXML
    private Button btnReports;
    @FXML
    private Button btnUsers;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged-in user
        currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            logger.error("No user session found in MainController");
            showLoginScreen();
            return;
        }

        // Display user information
        setupUserInterface();

        // Setup role-based access control
        setupRoleBasedAccess();

        // Load default view (Dashboard)
        loadDashboard();
    }

    /**
     * Setup user interface with current user information
     */
    private void setupUserInterface() {
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        userRoleLabel.setText("Role: " + currentUser.getRole());

        // Format and display last login
        if (currentUser.getLastLogin() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
            lastLoginLabel.setText("Last Login: " + currentUser.getLastLogin().format(formatter));
        } else {
            lastLoginLabel.setText("Last Login: First time login");
        }
    }

    /**
     * Setup role-based access control
     */
    private void setupRoleBasedAccess() {
        String role = currentUser.getRole();
        String roleNormalized = role == null ? "" : role.toLowerCase();

        switch (roleNormalized) {
            case "admin":
                // Admin has access to everything
                break;

            case "manager":
                // Manager has access to most features except user management
                btnUsers.setDisable(true);
                btnUsers.setVisible(false);
                break;

            case "staff":
                // Staff has limited access
                btnUsers.setDisable(true);
                btnUsers.setVisible(false);
                btnReports.setDisable(true);
                btnReports.setVisible(false);
                btnSuppliers.setDisable(true);
                btnSuppliers.setVisible(false);
                break;

            default:
                logger.warn("Unknown role: " + role);
                break;
        }
    }

    /**
     * Load Dashboard view
     */
    @FXML
    private void loadDashboard() {
        loadView("/fxml/DashboardView.fxml", "Dashboard");
    }

    /**
     * Load Products view
     */
    @FXML
    private void loadProducts() {
        loadView("/fxml/ProductView.fxml", "Products");
    }

    /**
     * Load Categories view
     */
    @FXML
    private void loadCategories() {
        loadView("/fxml/CategoryView.fxml", "Categories");
    }

    /**
     * Load Suppliers view
     */
    @FXML
    private void loadSuppliers() {
        loadView("/fxml/SupplierView.fxml", "Suppliers");
    }

    /**
     * Load Orders view
     */
    @FXML
    private void loadOrders() {
        loadView("/fxml/OrderView.fxml", "Orders");
    }

    /**
     * Load Reports view
     */
    @FXML
    private void loadReports() {
        loadView("/fxml/ReportView.fxml", "Reports");
    }

    /**
     * Load Users view
     */
    @FXML
    private void loadUsers() {
        loadView("/fxml/UserManagementView.fxml", "User Management");
    }

    /**
     * Generic method to load views into center pane
     */
    private void loadView(String fxmlPath, String viewName) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                logger.error("FXML resource not found: {}", fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Load Error", "View not found: " + viewName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            if (centerScroll != null) {
                centerScroll.setContent(view);
            } else {
                mainBorderPane.setCenter(view);
            }
            logger.info("Loaded view: " + viewName);
        } catch (IOException e) {
            logger.error("Failed to load view: " + viewName, e);
            showAlert(Alert.AlertType.ERROR, "Load Error",
                    "Failed to load " + viewName + " view");
        }
    }

    /**
     * Handle user profile menu item
     */
    @FXML
    private void handleProfile() {
        try {
            URL resource = getClass().getResource("/fxml/ProfileView.fxml");
            if (resource == null) {
                logger.error("Profile FXML resource not found: /fxml/ProfileView.fxml");
                showAlert(Alert.AlertType.ERROR, "Error", "Profile view not found");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage profileStage = new Stage();
            profileStage.setTitle("User Profile");
            profileStage.setScene(new Scene(root));
            profileStage.setResizable(false);
            profileStage.show();

        } catch (IOException e) {
            logger.error("Failed to load profile view", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open user profile");
        }
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        // Confirm logout
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to logout?");
        confirmAlert.setContentText("You will be returned to the login screen.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            performLogout();
        }
    }

    /**
     * Perform logout operation
     */
    private void performLogout() {
        try {
            logger.info("User {} logging out", currentUser.getUsername());

            // Clear session
            Session.getInstance().clearSession();

            // Show login screen
            showLoginScreen();

        } catch (Exception e) {
            logger.error("Error during logout", e);
            showAlert(Alert.AlertType.ERROR, "Logout Error",
                    "An error occurred during logout");
        }
    }

    /**
     * Show login screen
     */
    private void showLoginScreen() {
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginRoot = loader.load();

            Scene loginScene = new Scene(loginRoot);
            currentStage.setScene(loginScene);
            currentStage.setTitle("Inventory Management - Login");
            currentStage.centerOnScreen();

            logger.info("Navigated to login screen");

        } catch (IOException e) {
            logger.error("Failed to load login screen", e);
            showAlert(Alert.AlertType.ERROR, "Critical Error",
                    "Failed to return to login screen. Application will exit.");
            Platform.exit();
        }
    }

    /**
     * Handle about menu item
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Inventory Management System");
        alert.setHeaderText("Inventory Management System v1.0");
        alert.setContentText(
                "A comprehensive inventory management solution\n\n" +
                        "Logged in as: " + currentUser.getFullName() + "\n" +
                        "Role: " + currentUser.getRole() + "\n\n" +
                        "© 2024 Your Company. All rights reserved.");
        alert.showAndWait();
    }

    /**
     * Handle application exit
     */
    @FXML
    private void handleExit() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Exit Application");
        confirmAlert.setHeaderText("Are you sure you want to exit?");
        confirmAlert.setContentText("Any unsaved changes will be lost.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.info("Application exiting - User initiated");
            Platform.exit();
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}