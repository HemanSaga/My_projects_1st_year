package com.inventory;

/**
 * Launcher - Entry point for packaged JAR
 * This class is needed because JavaFX applications cannot be launched
 * directly from a JAR when the main class extends Application
 */
public class Launcher {

    public static void main(String[] args) {
        // Launch the JavaFX application
        InventoryApplication.main(args);
    }
}