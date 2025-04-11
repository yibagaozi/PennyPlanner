package org.softeng.group77.pennyplanner.repository.base;

import com.fasterxml.jackson.core.type.TypeReference;
import org.softeng.group77.pennyplanner.util.JsonFileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class JsonDataManager<T> {

    protected final String filePath;
    protected final TypeReference<List<T>> typeReference;

    protected JsonDataManager(String filePath, TypeReference<List<T>> typeReference) {
        this.filePath = filePath;
        this.typeReference = typeReference;
    }

    public T save(T entity) throws IOException {
        List<T> entities = loadAll();
        entities.add(entity);
        JsonFileUtil.writeToJson(filePath, entities);
        return entity;
    }

    public List<T> saveAll(List<T> newEntities) throws IOException {
        List<T> entities = loadAll();
        entities.addAll(newEntities);
        JsonFileUtil.writeToJson(filePath, entities);
        return entities;
    }

    public List<T> loadAll() throws IOException {
        return JsonFileUtil.readFromJson(filePath, typeReference);
    }

    public Optional<T> findOne(Predicate<T> matcher) throws IOException {
        return loadAll().stream().filter(matcher).findFirst();
    }

    public List<T> findAll(Predicate<T> matcher) throws IOException {
        return loadAll().stream().filter(matcher).collect(Collectors.toList());
    }

    public Optional<T> update(Predicate<T> matcher, T updatedEntity) throws IOException {
        List<T> entities = loadAll();

        for (int i = 0; i < entities.size(); i++) {
            if (matcher.test(entities.get(i))) {
                entities.set(i, updatedEntity);
                JsonFileUtil.writeToJson(filePath, entities);
                return Optional.of(updatedEntity);
            }
        }

        return Optional.empty();
    }

    public boolean delete(Predicate<T> matcher) throws IOException {
        List<T> entities = loadAll();
        List<T> remaining = entities.stream().filter(matcher.negate()).collect(Collectors.toList());

        if (remaining.size() != entities.size()) {
            JsonFileUtil.writeToJson(filePath, remaining);
            return true;
        }

        return false;
    }

    public boolean exists(Predicate<T> matcher) throws IOException {
        return loadAll().stream().anyMatch(matcher);
    }

    public long count() throws IOException {
        return loadAll().size();
    }

    public void clear() throws IOException {
        JsonFileUtil.writeToJson(filePath, new ArrayList<T>());
    }

}
