package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public interface TransactionService {

    TransactionDetail createTransaction(TransactionDetail transactionDetail);

    TransactionDetail updateTransaction(TransactionDetail transactionDetail);

    TransactionDetail getTransaction(String transactionId);

    boolean deleteTransaction(String transactionId);

    List<TransactionDetail> getUserTransactions(String userId);

    List<TransactionDetail> searchUserTransactions(String userId, String keyword);

    List<TransactionDetail> filterTransactionByDate(String userId, LocalDate startDate, LocalDate endDate);

    List<TransactionDetail> filterTransactionByCategory(String userId, String category);

}
