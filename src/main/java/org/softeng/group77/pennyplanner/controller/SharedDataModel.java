package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SharedDataModel {
    private static final ObservableList<tableModel> transactionData =
            FXCollections.observableArrayList();

    public static ObservableList<tableModel> getTransactionData() {
        return transactionData;
    }
}
