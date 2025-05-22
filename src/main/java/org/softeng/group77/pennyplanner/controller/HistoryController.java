package org.softeng.group77.pennyplanner.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.util.function.Predicate;

/**
 * Controller class for handling transaction history view.
 * Manages display and filtering of transaction records in a TableView with
 * filtering capabilities by year, month and category.
 */
public class HistoryController {
    
    /** Label displaying current month/year header */
    @FXML private Label date;
    
    /** ComboBox for selecting filter year */
    @FXML private ComboBox<Integer> Year;
    
    /** ComboBox for selecting filter month */
    @FXML private ComboBox<String> Month;
    
    /** ComboBox for selecting filter category */
    @FXML private ComboBox<String> category;
    
    /** TableView displaying transaction records */
    @FXML private TableView<tableModel> transactionTable;
    
    /** Column displaying transaction sequence number */
    @FXML private TableColumn<tableModel, String> transactionidColumn;
    
    /** Column displaying transaction date */
    @FXML private TableColumn<tableModel, String> dateColumn;
    
    /** Column displaying transaction description */
    @FXML private TableColumn<tableModel, String> descriptionColumn;
    
    /** Column displaying transaction amount with color coding */
    @FXML private TableColumn<tableModel, Double> amountColumn;
    
    /** Column displaying transaction category */
    @FXML private TableColumn<tableModel, String> categoryColumn;
    
    /** Column displaying payment method */
    @FXML private TableColumn<tableModel, String> methodColumn;
    
    /** SplitPane container for the view */
    @FXML private SplitPane splitPane;

    /**
     * The master list of all transactions (shared across application)
     */
    private final ObservableList<tableModel> transactionData = SharedDataModel.getTransactionData();
    
    /**
     * Filtered view of transaction data based on user selections
     */
    private FilteredList<tableModel> filteredData = new FilteredList<>(transactionData);

    /**
     * Initializes the controller after FXML loading. Sets up:
     * - ComboBox options for year/month/category filters
     * - TableView column bindings and formatting
     * - Dynamic filtering logic
     * - Special cell renderers for ID and amount columns
     */
    @FXML
    private void initialize() {
        // Initialize year selection options
        Year.setItems(FXCollections.observableArrayList(
                null, // Null option for no filter
                2022, 2023, 2024, 2025
        ));

        // Initialize month selection options
        Month.setItems(FXCollections.observableArrayList(
                null, // Null option for no filter  
                "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"
        ));

        // Initialize category selection options
        ObservableList<String> categories = FXCollections.observableArrayList(
                null, "Food", "Salary", "Living Bill", 
                "Entertainment", "Transportation", "Education", 
                "Clothes", "Others"
        );
        category.setItems(categories);

        // Bind table columns to model properties
        configureTableColumns();
        
        // Set default sorting by date descending
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        transactionTable.getSortOrder().add(dateColumn);

        // Configure amount column styling
        setupAmountColumnFormatter();
        
        // Configure dynamic row numbering
        setupRowNumbering();
        
        // Lock split pane divider position
        lockSplitPane();
        
        // Set up filter listeners
        setupFilterListeners();
    }

    /**
     * Binds table columns to corresponding model properties
     */
    private void configureTableColumns() {
        transactionidColumn.setCellValueFactory(cellData -> cellData.getValue().displayIdProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        methodColumn.setCellValueFactory(cellData -> cellData.getValue().methodProperty());
    }

    /**
     * Configures special formatting for amount column:
     * - Displays values with $ prefix and 2 decimal places
     * - Colors negative amounts red, positive green
     */
    private void setupAmountColumnFormatter() {
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", amount));
                    setStyle(amount < 0 ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                }
            }
        });
    }

    /**
     * Configures dynamic row numbering for the first column
     */
    private void setupRowNumbering() {
        transactionidColumn.setCellFactory(column -> new TableCell<tableModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        transactionidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
    }

    /**
     * Locks the split pane divider at 10% position
     */
    private void lockSplitPane() {
        splitPane.getDividers().forEach(divider -> 
            divider.positionProperty().addListener((obs, oldVal, newVal) -> 
                divider.setPosition(0.1)
            )
        );
    }

    /**
     * Sets up listeners for filter controls that trigger updateFilter()
     */
    private void setupFilterListeners() {
        Year.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        Month.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        category.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
    }

    /**
     * Updates the table filter based on current selections in:
     * - Year ComboBox
     * - Month ComboBox  
     * - Category ComboBox
     * 
     * Applies conjunctive (AND) filtering across all active filters
     */
    private void updateFilter() {
        Predicate<tableModel> predicate = transaction -> {
            // Date filtering logic
            boolean dateMatch = true;
            if (Year.getValue() != null || Month.getValue() != null) {
                String[] dateParts = transaction.getDate().split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);

                int selectedYear = Year.getValue() != null ? Year.getValue() : year;
                int selectedMonth = Month.getValue() != null ?
                        Integer.parseInt(Month.getValue()) : month;

                dateMatch = (year == selectedYear) && (month == selectedMonth);
            }

            // Category filtering logic
            boolean categoryMatch = category.getValue() == null ||
                    category.getValue().isEmpty() ||
                    transaction.getCategory().equals(category.getValue());

            return dateMatch && categoryMatch;
        };

        filteredData.setPredicate(predicate);
    }

    /**
     * Refreshes transaction data from shared model
     */
    @FXML
    public void refreshData() {
        SharedDataModel.refreshTransactionData();
    }

    // Navigation methods
    
    /**
     * Navigates to Home view
     * @throws IOException if FXML loading fails
     */
    @FXML
    private void turntoHome() throws IOException {
        MainApp.showHome();
    }
    
    /**
     * Navigates to Reports view
     * @throws IOException if FXML loading fails
     */
    @FXML
    private void turntoReport() throws IOException {
        MainApp.showReport();
    }
    
    /**
     * Navigates to History view (reloads current view)
     * @throws IOException if FXML loading fails
     */
    @FXML
    private void turntoHistory() throws IOException {
        MainApp.showhistory();
    }
    
    /**
     * Navigates to Management view
     * @throws IOException if FXML loading fails
     */
    @FXML
    private void turntoManagement() throws IOException {
        MainApp.showmanagement();
    }
    
    /**
     * Navigates to User profile view
     * @throws IOException if FXML loading fails
     */
    @FXML
    private void turntoUser() throws IOException {
        MainApp.showuser();
    }
    
    /**
     * Navigates to Login view
     * @throws IOException if FXML loading fails
     */
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
}
