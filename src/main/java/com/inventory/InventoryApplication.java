package com.inventory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.inventory.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InventoryApplication - Main JavaFX Application Class with Login
 * Entry point for the Inventory Management System
 */
public class InventoryApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(InventoryApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("==============================================");
            logger.info("Starting Inventory Management System...");
            logger.info("==============================================");

            // Test database connection first
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            if (!dbConnection.testConnection()) {
                logger.error("Database connection failed! Please check your database configuration.");
                showDatabaseError(primaryStage);
                return;
            }
            logger.info("✓ Database connection successful");

            // Load LOGIN view (changed from MainView)
            logger.info("Loading login view...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            logger.info("✓ Login view loaded successfully");

            // Create scene
            Scene scene = new Scene(root, 500, 650);

            // Load CSS stylesheet
            logger.info("Loading CSS stylesheet...");
            try {
                String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
                logger.info("✓ CSS stylesheet loaded: {}", cssPath);
            } catch (Exception e) {
                logger.warn("Could not load CSS stylesheet. Application will run with default styling.", e);
            }

            // Setup stage
            primaryStage.setTitle("Inventory Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Login window is not resizable
            primaryStage.centerOnScreen();

            // Show stage
            primaryStage.show();

            logger.info("==============================================");
            logger.info("✓ Application started successfully!");
            logger.info("==============================================");

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
            showStartupError(primaryStage, e);
        }
    }

    /**
     * Show database connection error dialog
     */
    private void showDatabaseError(Stage stage) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Failed to connect to database");
        alert.setContentText(
                "Please ensure:\n" +
                        "1. MySQL server is running\n" +
                        "2. Database 'inventory_management' exists\n" +
                        "3. Username and password are correct in application.properties\n\n" +
                        "Current settings:\n" +
                        "URL: jdbc:mysql://localhost:3306/inventory_management\n" +
                        "Username: root");
        alert.showAndWait();

        // Exit application
        System.exit(1);
    }

    /**
     * Show startup error dialog
     */
    private void showStartupError(Stage stage, Exception e) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Startup Error");
        alert.setHeaderText("Failed to start application");
        alert.setContentText(
                "Error: " + e.getMessage() + "\n\n" +
                        "Please check the console for detailed error logs.");
        alert.showAndWait();
    }

    @Override
    public void stop() {
        logger.info("Application shutting down...");
        // Cleanup code here if needed
        logger.info("Application stopped successfully");
    }

    public static void main(String[] args) {
        launch(args);
    }
}