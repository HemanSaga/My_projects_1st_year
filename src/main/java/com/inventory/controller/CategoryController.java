package com.inventory.controller;

import com.inventory.dao.CategoryDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.model.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * CategoryController - Manages category operations
 */
public class CategoryController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @FXML
    private TextField searchField;
    @FXML
    private Label totalCategoriesLabel;
    @FXML
    private Label activeCategoriesLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label categoryCountLabel;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private TableView<Category> categoriesTable;
    @FXML
    private TableColumn<Category, Integer> categoryIdCol;
    @FXML
    private TableColumn<Category, String> categoryNameCol;
    @FXML
    private TableColumn<Category, String> categoryDescriptionCol;
    @FXML
    private TableColumn<Category, Integer> categoryProductCountCol;
    @FXML
    private TableColumn<Category, String> categoryStatusCol;
    @FXML
    private TableColumn<Category, String> categoryCreatedCol;
    @FXML
    private TableColumn<Category, Void> categoryActionsCol;
    @FXML
    private Label selectedCategoryLabel;
    @FXML
    private Label selectedProductCountLabel;
    @FXML
    private Label selectedDescriptionLabel;
    @FXML
    private Label selectedStatusLabel;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private ObservableList<Category> allCategories = FXCollections.observableArrayList();
    private ObservableList<Category> filteredCategories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing CategoryController");

        setupTableColumns();
        // Ensure columns auto-size nicely
        categoriesTable.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
        setupSearchListener();
        setupSortOptions();
        setupTableSelectionListener();

        loadStatistics();
        loadCategories();
    }

    private void setupTableColumns() {
        categoryIdCol.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        categoryNameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Product count column
        categoryProductCountCol.setCellValueFactory(cellData -> {
            try {
                int count = productDAO.getProductCountByCategory(cellData.getValue().getCategoryId());
                return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
            } catch (SQLException e) {
                logger.error("Error getting product count", e);
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });

        // Status column
        categoryStatusCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("Active");
        });

        // Created date column
        categoryCreatedCol.setCellValueFactory(cellData -> {
            LocalDateTime created = cellData.getValue().getCreatedAt();
            String formatted = created != null ? created.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        // Actions column with buttons
        categoryActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                // Apply compact/icon button styles
                editBtn.getStyleClass().addAll("icon-button", "small-primary-button");
                deleteBtn.getStyleClass().addAll("icon-button", "small-danger-button");
                editBtn.setPrefWidth(36);
                deleteBtn.setPrefWidth(36);
                editBtn.setFocusTraversable(false);
                deleteBtn.setFocusTraversable(false);
                editBtn.setStyle("-fx-font-size:12px;");
                deleteBtn.setStyle("-fx-font-size:12px;");

                editBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    editCategory(category);
                });

                deleteBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(6, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }
    }

    private void setupSortOptions() {
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList(
                    "Name (A-Z)", "Name (Z-A)", "Newest First", "Oldest First"));
            sortCombo.setValue("Name (A-Z)");
            sortCombo.setOnAction(e -> applyFilters());
        }
    }

    private void setupTableSelectionListener() {
        categoriesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        updateSelectedCategoryDetails(newValue);
                    }
                });
    }

    private void loadStatistics() {
        try {
            List<Category> categories = categoryDAO.getAll();
            totalCategoriesLabel.setText(String.valueOf(categories.size()));
            activeCategoriesLabel.setText(String.valueOf(categories.size()));

            int totalProducts = productDAO.getAll().size();
            totalProductsLabel.setText(String.valueOf(totalProducts));

        } catch (SQLException e) {
            logger.error("Error loading statistics", e);
            showError("Failed to load statistics: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            logger.info("Loading categories from database...");
            List<Category> categories = categoryDAO.getAll();
            logger.info("✓ Loaded {} categories", categories.size());

            allCategories.clear();
            allCategories.addAll(categories);

            filteredCategories.clear();
            filteredCategories.addAll(categories);

            categoriesTable.setItems(filteredCategories);
            updateCategoryCount();

        } catch (SQLException e) {
            logger.error("Error loading categories", e);
            showError("Failed to load categories: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String search = searchField != null ? searchField.getText().toLowerCase() : "";
        String sortOption = sortCombo != null ? sortCombo.getValue() : "Name (A-Z)";

        List<Category> filtered = allCategories.stream()
                .filter(c -> matchesSearch(c, search))
                .collect(Collectors.toList());

        // Apply sorting
        switch (sortOption) {
            case "Name (Z-A)":
                filtered.sort((c1, c2) -> c2.getCategoryName().compareTo(c1.getCategoryName()));
                break;
            case "Newest First":
                filtered.sort((c1, c2) -> {
                    if (c1.getCreatedAt() == null || c2.getCreatedAt() == null)
                        return 0;
                    return c2.getCreatedAt().compareTo(c1.getCreatedAt());
                });
                break;
            case "Oldest First":
                filtered.sort((c1, c2) -> {
                    if (c1.getCreatedAt() == null || c2.getCreatedAt() == null)
                        return 0;
                    return c1.getCreatedAt().compareTo(c2.getCreatedAt());
                });
                break;
            default: // Name (A-Z)
                filtered.sort((c1, c2) -> c1.getCategoryName().compareTo(c2.getCategoryName()));
        }

        filteredCategories.clear();
        filteredCategories.addAll(filtered);
        updateCategoryCount();
    }

    private boolean matchesSearch(Category category, String search) {
        if (search.isEmpty())
            return true;
        return category.getCategoryName().toLowerCase().contains(search) ||
                (category.getDescription() != null && category.getDescription().toLowerCase().contains(search));
    }

    private void updateCategoryCount() {
        categoryCountLabel.setText("Showing " + filteredCategories.size() + " categories");
    }

    private void updateSelectedCategoryDetails(Category category) {
        selectedCategoryLabel.setText(category.getCategoryName());
        selectedDescriptionLabel.setText(category.getDescription() != null ? category.getDescription() : "--");
        selectedStatusLabel.setText("Active");

        try {
            int productCount = productDAO.getProductCountByCategory(category.getCategoryId());
            selectedProductCountLabel.setText(String.valueOf(productCount));
        } catch (SQLException e) {
            logger.error("Error getting product count", e);
            selectedProductCountLabel.setText("--");
        }
    }

    @FXML
    private void addCategory() {
        logger.info("Add category clicked");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Create New Category");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Category Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Category name is required");
                    return;
                }

                Category category = new Category(name, descriptionArea.getText().trim());
                boolean success = categoryDAO.add(category);

                if (success) {
                    loadStatistics();
                    loadCategories();
                    showInfo("Category added successfully!");
                }
            } catch (Exception e) {
                logger.error("Error adding category", e);
                showError("Error: " + e.getMessage());
            }
        }
    }

    private void editCategory(Category category) {
        logger.info("Edit category: {}", category.getCategoryName());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit Category: " + category.getCategoryName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(category.getCategoryName());
        TextArea descriptionArea = new TextArea(category.getDescription());
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Category Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Category name is required");
                    return;
                }

                category.setCategoryName(name);
                category.setDescription(descriptionArea.getText().trim());

                boolean success = categoryDAO.update(category);

                if (success) {
                    loadStatistics();
                    loadCategories();
                    showInfo("Category updated successfully!");
                }
            } catch (Exception e) {
                logger.error("Error updating category", e);
                showError("Error: " + e.getMessage());
            }
        }
    }

    private void deleteCategory(Category category) {
        logger.info("Delete category: {}", category.getCategoryName());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete " + category.getCategoryName() + "?");
        alert.setContentText("This action cannot be undone. Products in this category will not be deleted.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = categoryDAO.delete(category.getCategoryId());

                if (success) {
                    loadStatistics();
                    loadCategories();
                    showInfo("Category deleted successfully!");
                }
            } catch (Exception e) {
                logger.error("Error deleting category", e);
                showError("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void editSelectedCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editCategory(selected);
        } else {
            showError("Please select a category to edit");
        }
    }

    @FXML
    private void deleteSelectedCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteCategory(selected);
        } else {
            showError("Please select a category to delete");
        }
    }

    @FXML
    private void viewCategoryProducts() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showInfo("View products feature coming soon for: " + selected.getCategoryName());
        } else {
            showError("Please select a category");
        }
    }

    @FXML
    private void exportCategories() {
        try {
            File file = new File("categories_export.csv");
            FileWriter writer = new FileWriter(file);

            writer.write("ID,Name,Description,Created Date\n");

            for (Category c : filteredCategories) {
                String created = c.getCreatedAt() != null
                        ? c.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "N/A";

                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\"\n",
                        c.getCategoryId(),
                        c.getCategoryName(),
                        c.getDescription() != null ? c.getDescription() : "",
                        created));
            }

            writer.close();
            showInfo("Categories exported to: " + file.getAbsolutePath());

        } catch (Exception e) {
            logger.error("Error exporting categories", e);
            showError("Failed to export: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}