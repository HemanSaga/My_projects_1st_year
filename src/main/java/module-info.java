module inventory.management.system {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // Database
    requires java.sql;

    // Logging
    requires org.slf4j;

    // BCrypt - ADD THIS LINE
    requires jbcrypt;

    // Open packages to JavaFX for reflection
    opens com.inventory to javafx.fxml;
    opens com.inventory.controller to javafx.fxml;
    opens com.inventory.model to javafx.base;

    // Export packages
    exports com.inventory;
    exports com.inventory.controller;
    exports com.inventory.model;
    exports com.inventory.util;
    exports com.inventory.service;
    exports com.inventory.dao;
}