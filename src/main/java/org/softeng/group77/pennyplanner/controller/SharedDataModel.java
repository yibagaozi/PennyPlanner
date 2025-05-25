package org.softeng.group77.pennyplanner.controller;

import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Shared data model that provides centralized access to transaction data across the application.
 *
 * @author CHAI Jiayang
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 1.1.0
 */
@Component
public class SharedDataModel {
    private static TransactionAdapter transactionAdapterStatic;
    private static final ObservableList<tableModel> transactionData = FXCollections.observableArrayList();
    private static AuthService authServiceStatic;
    private static boolean dataInitialized = false;

//    @Autowired
//    private static AuthService authService;
//    private static String currentUserId = null;

//    public static void setTransactionAdapter(TransactionAdapter adapter) {
//        transactionAdapter = adapter;
//    }
    // 实例成员，用于 Spring 注入
    private final TransactionAdapter transactionAdapter;
    private final AuthService authService;

//    public static void setAuthService(AuthService service) {
//        authService = service;
////        currentUserId = null;  // 重置当前用户ID
////        dataInitialized = false;  // 标记数据需要重新初始化
//    }

    /**
     * Creates a new SharedDataModel with the specified transaction adapter and auth service.
     *
     * @param transactionAdapter adapter for transaction data persistence
     * @param authService service for user authentication and identity management
     */
    @Autowired
    public SharedDataModel(TransactionAdapter transactionAdapter, AuthService authService) {
        this.transactionAdapter = transactionAdapter;
        this.authService = authService;
    }

    /**
     * Initializes static references after Spring constructs the instance.
     * This method is automatically called by Spring after dependency injection.
     */
    @PostConstruct
    private void init() {
        // 将注入的实例赋值给静态变量
        transactionAdapterStatic = this.transactionAdapter;
        authServiceStatic = this.authService;
        System.out.println("SharedDataModel 初始化完成: " +
                (transactionAdapterStatic != null) + ", " + (authServiceStatic != null));
    }

    /**
     * Gets the observable list of transaction data for the current user.
     *
     * @return an observable list of transaction models
     */
    public static ObservableList<tableModel> getTransactionData() {
        //refreshTransactionData();
        return transactionData;
    }

//    private static void checkAndRefreshUserData() {
//        if (authService != null) {
//            try {
//                String userId = authService.getCurrentUser().getId();
//                // 如果用户ID变化或尚未初始化数据，则刷新数据
//                if (currentUserId == null || !currentUserId.equals(userId) || !dataInitialized) {
//                    currentUserId = userId;
//                    refreshTransactionData();
//                }
//            } catch (Exception e) {
//                // 用户未登录或获取用户ID失败
//                if (currentUserId != null) {
//                    // 清空之前用户的数据
//                    transactionData.clear();
//                    currentUserId = null;
//                }
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * Refreshes transaction data from the persistence layer for the current user.
     */
    public static void refreshTransactionData() {
        if (transactionAdapterStatic != null && authServiceStatic.getCurrentUser() != null) {
            try {
                String userId = authServiceStatic.getCurrentUser().getId();
                if (userId != null) {
                    transactionData.clear();
                    ObservableList<tableModel> userTransactions = transactionAdapterStatic.getUserTransactions();
                    transactionData.addAll(userTransactions);
                    dataInitialized = true;
                } else {
                    System.out.println("Not Login Yet");
                    transactionData.clear();
                }
            }catch (Exception e) {
                System.out.println("fail to refresh: " + e.getMessage());
                e.printStackTrace();
                transactionData.clear();
            }
        }

    }

    /**
     * Adds a new transaction to the system.
     *
     * @param transaction the transaction model to add
     * @return true if the operation was successful, false otherwise
     */
    public static boolean addTransaction(tableModel transaction) {
        boolean success = false;
        if (transactionAdapterStatic != null) {
            success = transactionAdapterStatic.saveTransaction(transaction);
            if (success) {
                // 刷新数据以确保序号连续
                refreshTransactionData();
            }
        } else {
            transaction.setDisplayId(String.valueOf(transactionData.size() + 1));
            transactionData.add(transaction);
            success = true;
            System.out.println("TransactionAdapter is not initialized yet，Data's only in memory ");
        }
        return success;
    }

    /**
     * Updates an existing transaction in the system.
     *
     * @param transaction the updated transaction model
     * @return true if the operation was successful, false otherwise
     */
    public static boolean updateTransaction(tableModel transaction) {
        boolean success = false;
        if (transactionAdapterStatic != null) {
            success = transactionAdapterStatic.updateTransaction(transaction);
            if (success) {
                refreshTransactionData(); // 刷新数据
            }
        }
        return success;
    }

    /**
     * Deletes a transaction from the system by its ID.
     *
     * @param transactionId the ID of the transaction to delete
     * @return true if the operation was successful, false otherwise
     */
    public static boolean deleteTransaction(String transactionId) {
        boolean success = false;
        if (transactionAdapterStatic != null) {
            success = transactionAdapterStatic.deleteTransaction(transactionId);
            if (success) {
                transactionData.removeIf(t -> t.getId().equals(transactionId));
            }
        }
        return success;
    }

    /**
     * Clears the UI data without affecting backend storage.
     * It marks the data as needing reinitialization on next access.
     */
    public static void clearUIData() {
        transactionData.clear(); // 只清除UI数据集合
        dataInitialized = false; // 标记需要重新初始化
    }
}
