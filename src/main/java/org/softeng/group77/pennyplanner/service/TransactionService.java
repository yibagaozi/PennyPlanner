package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public interface TransactionService {

    TransactionDetail createTransaction(TransactionDetail transactionDetail);

    TransactionDetail updateTransaction(TransactionDetail transactionDetail);

    TransactionDetail getTransaction(String transactionId);

    boolean deleteTransaction(String transactionId);

    List<TransactionDetail> getUserTransactions();

    List<TransactionDetail> searchUserTransactions(String keyword);

    List<TransactionDetail> filterTransactionByDate(LocalDate startDate, LocalDate endDate);

    List<TransactionDetail> filterTransactionByCategory(String category);
     Map<String, Double> getDefaultSummary( LocalDateTime endTime);

     Map<String, Double> getSummaryByDateRange(LocalDate startDate, LocalDate endDate);

     Map<String, Double> calculateSummary(List<Transaction> transactions);
}
