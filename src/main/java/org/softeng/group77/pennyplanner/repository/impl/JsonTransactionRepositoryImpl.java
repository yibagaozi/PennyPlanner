package org.softeng.group77.pennyplanner.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.repository.TransactionRepository;
import org.softeng.group77.pennyplanner.repository.base.JsonDataManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class JsonTransactionRepositoryImpl<T> extends JsonDataManager<T> implements TransactionRepository {

    public JsonTransactionRepositoryImpl(@Value("${app.data.path:data}/transaction.json") String filePath) {
        super(filePath, new TypeReference<>() {});
    }
}
