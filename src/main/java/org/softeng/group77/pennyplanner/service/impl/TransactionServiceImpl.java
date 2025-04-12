package org.softeng.group77.pennyplanner.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    @Override
    public TransactionDetail createTransaction(TransactionDetail transactionDetail) {
        return null;
    }

    @Override
    public TransactionDetail updateTransaction(TransactionDetail transactionDetail) {
        return null;
    }

    @Override
    public TransactionDetail getTransaction(String transactionId) {
        return null;
    }

    @Override
    public boolean deleteTransaction(String transactionId) {
        return false;
    }

    @Override
    public List<TransactionDetail> getUserTransactions(String userId) {
        return List.of();
    }

    @Override
    public List<TransactionDetail> searchUserTransactions(String userId, String keyword) {
        return List.of();
    }

    @Override
    public List<TransactionDetail> filterTransactionByDate(String userId, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public List<TransactionDetail> filterTransactionByCategory(String userId, String category) {
        return List.of();
    }
}
