package org.softeng.group77.pennyplanner.util;

import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;

import java.util.List;

public class TransactionMapper {

    public TransactionDetail toTransactionDetail(Transaction transaction) {
        if (transaction == null) return null;
        return new TransactionDetail(transaction);
    }

    public List<TransactionDetail> toTransactionDetailList(List<Transaction> transactions) {
        if (transactions == null) return null;
        return transactions.stream()
                .map(this::toTransactionDetail)
                .toList();
    }

}