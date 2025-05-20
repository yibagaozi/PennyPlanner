package org.softeng.group77.pennyplanner.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.repository.TransactionRepository;
import org.softeng.group77.pennyplanner.repository.base.JsonDataManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class JsonTransactionRepositoryImpl extends JsonDataManager<Transaction> implements TransactionRepository {

    public JsonTransactionRepositoryImpl(@Value("${app.data.path:data}/transaction.json") String filePath) {
        super(filePath, new TypeReference<>() {});
    }

    @Override
    public Transaction save(Transaction transaction) throws IOException {
        delete(existing -> existing.getId().equals(transaction.getId()));
        return super.save(transaction);
    }

    @Override
    public boolean deleteById(String id) throws IOException {
        return delete(transaction -> transaction.getId().equals(id));
    }

    @Override
    public Optional<Transaction> findById(String id) throws IOException {
        return findOne(transaction -> transaction.getId().equals(id));
    }

    @Override
    public List<Transaction> findByUserId(String userId) throws IOException {
        return findAll(transaction -> transaction.getUserId() != null && transaction.getUserId().equals(userId));
    }

    @Override
    public List<Transaction> findByUserIdAndDescription(String userId, String description) throws IOException {
        return findAll(transaction ->
        transaction.getUserId() != null &&
        transaction.getUserId().equals(userId) &&
        transaction.getDescription() != null &&
        transaction.getDescription().toLowerCase().contains(description.toLowerCase())
        );
    }

    @Override
    public List<Transaction> findByUserIdAndTransactionDateTimeBetween(String userId, LocalDateTime start, LocalDateTime end) throws IOException {
        return findAll(transaction ->
            transaction.getUserId() != null &&
            transaction.getUserId().equals(userId) &&
            transaction.getTransactionDateTime() != null &&
            !transaction.getTransactionDateTime().isBefore(start) &&
            !transaction.getTransactionDateTime().isAfter(end)
        );
    }

}
