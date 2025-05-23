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
 * SharedDataModel 用于管理和提供交易数据的访问和操作，包括数据的刷新、添加、更新、删除等操作。
 * 该类确保交易数据始终保持最新状态，尤其是根据当前用户的身份信息动态加载相关交易数据。
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

    @Autowired
    public SharedDataModel(TransactionAdapter transactionAdapter, AuthService authService) {
        this.transactionAdapter = transactionAdapter;
        this.authService = authService;
    }

    @PostConstruct
    private void init() {
        // 将注入的实例赋值给静态变量
        transactionAdapterStatic = this.transactionAdapter;
        authServiceStatic = this.authService;
        System.out.println("SharedDataModel 初始化完成: " +
                (transactionAdapterStatic != null) + ", " + (authServiceStatic != null));
    }

    /**
     * 获取当前用户的交易数据。如果需要，数据将会被刷新。
     * 
     * @return 当前用户的交易数据列表
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
     * 刷新当前用户的交易数据，确保数据是最新的。
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
     * 添加一笔新的交易记录。
     * 
     * @param transaction 需要添加的交易记录
     * @return 操作是否成功
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
     * 更新一笔已存在的交易记录。
     * 
     * @param transaction 需要更新的交易记录
     * @return 操作是否成功
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
     * 删除一笔交易记录。
     * 
     * @param transactionId 需要删除的交易记录的ID
     * @return 操作是否成功
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
     * 清除 UI 数据，不清除后端数据。
     * 仅清除界面上显示的交易数据，并标记数据为未初始化状态。
     */
    public static void clearUIData() {
        transactionData.clear(); // 只清除UI数据集合
        dataInitialized = false; // 标记需要重新初始化
    }
}
