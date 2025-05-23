package org.softeng.group77.pennyplanner.repository;

import org.softeng.group77.pennyplanner.model.Transaction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save (Transaction transaction) throws IOException;

    boolean deleteById(String id) throws IOException;

    Optional<Transaction> findById(String id) throws IOException;

    List<Transaction> findByUserId(String userId) throws IOException;
    List<Transaction> findByUserIdAndDescription(String userId, String description) throws IOException;
    List<Transaction> findByUserIdAndTransactionDateTimeBetween(
        String userId,
        LocalDateTime start,
        LocalDateTime end
) throws IOException;

}
