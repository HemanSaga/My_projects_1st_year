# Inventory Management System

A comprehensive JavaFX-based inventory management system for tracking products, stock levels, suppliers, and generating analytics reports.

## Features

- **Dashboard**: Real-time statistics, charts, and recent transaction history
- **Product Management**: Complete CRUD operations for products with search and filtering
- **Stock Management**: Track stock in/out transactions with detailed history
- **Low Stock Alerts**: Automatic alerts for products below threshold levels
- **Category Management**: Organize products into categories
- **Supplier Management**: Manage supplier information and relationships
- **Reports & Analytics**: Generate comprehensive reports with charts and export capabilities

## Technology Stack

- **Java 11+**
- **JavaFX** - UI Framework
- **MySQL** - Database
- **Maven** - Build Tool
- **SLF4J + Logback** - Logging

## Prerequisites

- JDK 11 or higher
- MySQL 8.0 or higher
- Maven 3.6+

## Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE inventory_db;
```

2. Run the SQL scripts to create tables

3. Update `src/main/resources/application.properties` with your database credentials:
```properties
db.url=jdbc:mysql://localhost:3306/inventory_db
db.username=your_username
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

## How to Run

1. Configure MySQL and create database
2. Update `src/main/resources/application.properties`
3. Run the SQL script `src/main/resources/update_user_passwords.sql` to ensure test users are created with the correct hashes (defaults use password `password123`).
4. Run:
   ```bash
   mvn clean install
   mvn javafx:run
   ```

You can also run the diagnostic tool to confirm password hashes in your DB:

```bash
# compile and run
mvn -q -DskipTests package
java -cp target/classes;target/dependency/* com.inventory.util.TestLogin
```

## Version

Current Version: 1.0.0
