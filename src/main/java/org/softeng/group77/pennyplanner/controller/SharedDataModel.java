package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.stereotype.Component;

/**
 * SharedDataModel 用于管理和提供交易数据的访问和操作，包括数据的刷新、添加、更新、删除等操作。
 * 该类确保交易数据始终保持最新状态，尤其是根据当前用户的身份信息动态加载相关交易数据。
 */
@Component
public class SharedDataModel {
    
    private static TransactionAdapter transactionAdapter;
    private static final ObservableList<tableModel> transactionData = FXCollections.observableArrayList();
    private static boolean dataInitialized = false;
    private static AuthService authService;
    private static String currentUserId = null;

    /**
     * 设置 TransactionAdapter，用于与后端交互操作交易数据。
     * 
     * @param adapter TransactionAdapter 实例
     */
    public static void setTransactionAdapter(TransactionAdapter adapter) {
        transactionAdapter = adapter;
    }

    /**
     * 设置 AuthService，用于获取当前用户信息。
     * 
     * @param service AuthService 实例
     */
    public static void setAuthService(AuthService service) {
        authService = service;
    }

    /**
     * 获取当前用户的交易数据。如果需要，数据将会被刷新。
     * 
     * @return 当前用户的交易数据列表
     */
    public static ObservableList<tableModel> getTransactionData() {
        checkAndRefreshUserData(); // 检查并刷新用户数据
        return transactionData;
    }

    /**
     * 检查当前用户的身份信息，并在用户身份变化时刷新交易数据。
     */
    private static void checkAndRefreshUserData() {
        if (authService != null) {
            try {
                String userId = authService.getCurrentUser().getId();
                // 如果用户ID变化或尚未初始化数据，则刷新数据
                if (currentUserId == null || !currentUserId.equals(userId) || !dataInitialized) {
                    currentUserId = userId;
                    refreshTransactionData(); // 刷新交易数据
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

    /**
     * 刷新当前用户的交易数据，确保数据是最新的。
     */
    public static void refreshTransactionData() {
        if (transactionAdapter != null && currentUserId != null) {
            try {
                String userId = authService.getCurrentUser().getId();
                if (userId != null) {
                    transactionData.clear();
                    ObservableList<tableModel> userTransactions = transactionAdapter.getUserTransactions();
                    transactionData.addAll(userTransactions);
                    dataInitialized = true;
                } else {
                    System.out.println("用户未登录，无法刷新交易数据");
                    transactionData.clear();
                }
            } catch (Exception e) {
                System.out.println("刷新交易数据失败: " + e.getMessage());
                e.printStackTrace();
                transactionData.clear();
            }
        }
    }

    /**
     * 添加一笔新的交易记录。
     * 
     * @param transaction 需要添加的交易记录
     * @return 操作是否成功
     */
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

    /**
     * 更新一笔已存在的交易记录。
     * 
     * @param transaction 需要更新的交易记录
     * @return 操作是否成功
     */
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

    /**
     * 删除一笔交易记录。
     * 
     * @param transactionId 需要删除的交易记录的ID
     * @return 操作是否成功
     */
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

    /**
     * 清除 UI 数据，不清除后端数据。
     * 仅清除界面上显示的交易数据，并标记数据为未初始化状态。
     */
    public static void clearUIData() {
        transactionData.clear(); // 只清除UI数据集合
        dataInitialized = false; // 标记需要重新初始化
    }
}

