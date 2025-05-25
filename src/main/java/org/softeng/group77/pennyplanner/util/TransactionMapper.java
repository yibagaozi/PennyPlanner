package org.softeng.group77.pennyplanner.util;

import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper utility for converting between Transaction model objects and DTOs.
 * Provides methods to transform internal data models to data transfer objects
 * for use in the presentation layer.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
@Component
public class TransactionMapper {

    /**
     * Converts a Transaction model to a TransactionDetail DTO
     *
     * @param transaction the transaction model to convert
     * @return the corresponding DTO or null if input is null
     */
    public TransactionDetail toTransactionDetail(Transaction transaction) {
        if (transaction == null) return null;
        return new TransactionDetail(transaction);
    }

    /**
     * Converts a list of Transaction models to a list of TransactionDetail DTOs
     *
     * @param transactions the list of transaction models to convert
     * @return a list of corresponding DTOs or null if input is null
     */
    public List<TransactionDetail> toTransactionDetailList(List<Transaction> transactions) {
        if (transactions == null) return null;
        return transactions.stream()
                .map(this::toTransactionDetail)
                .toList();
    }

}