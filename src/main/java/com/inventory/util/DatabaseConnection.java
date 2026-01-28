package com.inventory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection - Singleton class to manage database connections
 * Reads database configuration from application.properties
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static DatabaseConnection instance;

    private String url;
    private String username;
    private String password;
    private String driver;

    /**
     * Private constructor - loads database configuration
     */
    private DatabaseConnection() {
        loadDatabaseProperties();
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Load database properties from application.properties
     */
    private void loadDatabaseProperties() {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                logger.error("Unable to find application.properties");
                throw new RuntimeException("application.properties not found");
            }

            props.load(input);

            // Read database properties
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
            this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

            // Load JDBC driver
            Class.forName(driver);

            logger.info("Database configuration loaded successfully");

        } catch (IOException e) {
            logger.error("Error loading database properties", e);
            throw new RuntimeException("Failed to load database configuration", e);
        } catch (ClassNotFoundException e) {
            logger.error("JDBC driver not found: {}", driver, e);
            throw new RuntimeException("JDBC driver not found", e);
        }
    }

    /**
     * Get a new database connection
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            logger.debug("Database connection established");
            return conn;
        } catch (SQLException e) {
            logger.error("Error establishing database connection", e);
            throw e;
        }
    }

    /**
     * Release a database connection
     */
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.debug("Database connection released");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}
