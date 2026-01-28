package com.inventory.controller;

import com.inventory.model.User;
import com.inventory.service.AuthenticationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginController - ENHANCED DEBUG VERSION
 * Provides detailed logging for troubleshooting login issues
 */
public class LoginController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private ProgressIndicator loadingIndicator;

    private final AuthenticationService authService;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("===========================================");
        logger.info("LoginController Initialized");
        logger.info("===========================================");

        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // Hide loading indicator initially
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        // Add enter key support for login
        setupEnterKeySupport();

        // Focus on username field
        Platform.runLater(() -> {
            if (usernameField != null) {
                usernameField.requestFocus();
            }
        });
        
        logger.info("LoginController initialization complete");
    }

    /**
     * Setup Enter key support for login
     */
    private void setupEnterKeySupport() {
        if (usernameField != null) {
            usernameField.setOnKeyPressed(this::handleKeyPress);
        }
        if (passwordField != null) {
            passwordField.setOnKeyPressed(this::handleKeyPress);
        }
    }

    /**
     * Handle key press events
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Handle login button click - ENHANCED WITH DEBUG LOGGING
     */
    @FXML
    private void handleLogin() {
        logger.info("===========================================");
        logger.info("LOGIN ATTEMPT STARTED");
        logger.info("===========================================");
        
        // Get credentials
        String username = usernameField.getText();
        String password = passwordField.getText();

        // DEBUG: Log input (without actual password)
        logger.info("Username entered: '{}'", username);
        logger.info("Password length: {}", password != null ? password.length() : 0);
        logger.info("Username trimmed: '{}'", username != null ? username.trim() : "null");

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login validation failed: Empty username");
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.isEmpty()) {
            logger.warn("Login validation failed: Empty password");
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        logger.info("Input validation passed");

        // Show loading
        setLoading(true);

        // Perform login in background thread to keep UI responsive
        new Thread(() -> {
            try {
                logger.info("Calling AuthenticationService.login()...");
                logger.info("Username being used: '{}'", username.trim());

                // Authenticate user
                User user = authService.login(username, password);

                logger.info("AuthenticationService.login() returned: {}", user != null ? "User object" : "null");

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    setLoading(false);

                    if (user != null) {
                        logger.info("===========================================");
                        logger.info("LOGIN SUCCESSFUL");
                        logger.info("User: {}", user.getUsername());
                        logger.info("Role: {}", user.getRole());
                        logger.info("===========================================");
                        openMainWindow();
                    } else {
                        logger.warn("===========================================");
                        logger.warn("LOGIN FAILED - Invalid credentials");
                        logger.warn("Username attempted: '{}'", username);
                        logger.warn("===========================================");
                        showError("Invalid username or password");
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });

            } catch (Exception e) {
                logger.error("===========================================");
                logger.error("LOGIN ERROR - Exception occurred", e);
                logger.error("Error type: {}", e.getClass().getName());
                logger.error("Error message: {}", e.getMessage());
                logger.error("===========================================");
                
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("An error occurred: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Open main application window
     */
    private void openMainWindow() {
        try {
            logger.info("Loading main application window...");

            // Load main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            // Create new scene
            Scene scene = new Scene(root, 1400, 900);

            // Load CSS
            try {
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
                logger.info("CSS loaded successfully");
            } catch (Exception e) {
                logger.warn("Could not load CSS", e);
            }

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set new scene
            stage.setScene(scene);
            stage.setTitle("Inventory Management System v1.0");
            stage.setMaximized(true);
            stage.setMinWidth(1200);
            stage.setMinHeight(700);

            logger.info("Main window loaded successfully");

        } catch (Exception e) {
            logger.error("Error loading main window", e);
            showError("Failed to load application. Please contact support.");
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            // Hide error after 5 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> {
                        if (errorLabel != null) {
                            errorLabel.setVisible(false);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        if (loginButton != null) {
            loginButton.setDisable(loading);
        }
        if (usernameField != null) {
            usernameField.setDisable(loading);
        }
        if (passwordField != null) {
            passwordField.setDisable(loading);
        }
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
    }

    /**
     * Handle cancel/exit button
     */
    @FXML
    private void handleCancel() {
        logger.info("Login cancelled by user");
        Platform.exit();
    }

    /**
     * Handle forgot password
     */
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Reset");
        alert.setContentText("Please contact your system administrator to reset your password.");
        alert.showAndWait();
    }
}