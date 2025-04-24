package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class SharedDataModel {
    private static TransactionAdapter transactionAdapter;
    private static final ObservableList<tableModel> transactionData = FXCollections.observableArrayList();
    private static boolean dataInitialized = false;
    private static AuthService authService;
    private static String currentUserId = null;

    public static void setTransactionAdapter(TransactionAdapter adapter) {
        transactionAdapter = adapter;
    }

    public static void setAuthService(AuthService service) {
        authService = service;
//        currentUserId = null;  // 重置当前用户ID
//        dataInitialized = false;  // 标记数据需要重新初始化
    }

    //Get Transaction Data
    public static ObservableList<tableModel> getTransactionData() {
        checkAndRefreshUserData();
        return transactionData;
    }

    private static void checkAndRefreshUserData() {
        if (authService != null) {
            try {
                String userId = authService.getCurrentUser().getId();
                // 如果用户ID变化或尚未初始化数据，则刷新数据
                if (currentUserId == null || !currentUserId.equals(userId) || !dataInitialized) {
                    currentUserId = userId;
                    refreshTransactionData();
                }
            } catch (Exception e) {
                // 用户未登录或获取用户ID失败
                if (currentUserId != null) {
                    // 清空之前用户的数据
                    transactionData.clear();
                    currentUserId = null;
                }
                e.printStackTrace();
            }
        }
    }

    // Refresh Data
    public static void refreshTransactionData() {
        if (transactionAdapter != null && currentUserId != null) {
            try {
                String userId = authService.getCurrentUser().getId();
                if (userId != null) {
                    transactionData.clear();
                    ObservableList<tableModel> userTransactions = transactionAdapter.getUserTransactions();
                    transactionData.addAll(userTransactions);
                    // 添加到UI数据集合
                    transactionData.addAll(userTransactions);
                    dataInitialized = true;
                } else {
                    System.out.println("用户未登录，无法刷新交易数据");
                    transactionData.clear();
                }
            }catch (Exception e) {
                System.out.println("刷新交易数据失败: " + e.getMessage());
                e.printStackTrace();
                transactionData.clear();
            }
        }

    }

    // Add Entry
    public static boolean addTransaction(tableModel transaction) {
        boolean success = false;
        if (transactionAdapter != null) {
            success = transactionAdapter.saveTransaction(transaction);
            if (success) {
                // 刷新数据以确保序号连续
                refreshTransactionData();
            }
        } else {
            transaction.setDisplayId(String.valueOf(transactionData.size() + 1));
            transactionData.add(transaction);
            success = true;
            System.out.println("警告：TransactionAdapter未初始化，数据仅保存在内存中");
        }
        return success;
    }

    // Update
    public static boolean updateTransaction(tableModel transaction) {
        boolean success = false;
        if (transactionAdapter != null) {
            success = transactionAdapter.updateTransaction(transaction);
            if (success) {
                refreshTransactionData(); // 刷新数据
            }
        }
        return success;
    }


    //Remove Data
    public static boolean deleteTransaction(String transactionId) {
        boolean success = false;
        if (transactionAdapter != null) {
            success = transactionAdapter.deleteTransaction(transactionId);
            if (success) {
                transactionData.removeIf(t -> t.getId().equals(transactionId));
            }
        }
        return success;
    }

    // 清除UI数据 ，而不是后端数据
    public static void clearUIData() {
        transactionData.clear(); // 只清除UI数据集合
        dataInitialized = false; // 标记需要重新初始化
    }
}
