package org.softeng.group77.pennyplanner.repository.base;

import com.fasterxml.jackson.core.type.TypeReference;
import org.softeng.group77.pennyplanner.util.JsonFileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base repository implementation for JSON file-based data storage.
 * Provides generic CRUD operations for entity types, handling
 * persistence through JSON serialization.
 *
 * @param <T> the entity type this manager handles
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
public abstract class JsonDataManager<T> {

    /** Path to the JSON file where entities are stored */
    protected final String filePath;

    /** Type reference for deserialization from JSON */
    protected final TypeReference<List<T>> typeReference;

    /**
     * Creates a new JSON data manager
     *
     * @param filePath the path to the JSON file for entity storage
     * @param typeReference the type reference for JSON serialization/deserialization
     */
    protected JsonDataManager(String filePath, TypeReference<List<T>> typeReference) {
        this.filePath = filePath;
        this.typeReference = typeReference;
    }

    /**
     * Saves a new entity to the JSON file
     *
     * @param entity the entity to save
     * @return the saved entity
     * @throws IOException if a file access error occurs
     */
    public T save(T entity) throws IOException {
        List<T> entities = loadAll();
        entities.add(entity);
        JsonFileUtil.writeToJson(filePath, entities);
        return entity;
    }

    /**
     * Saves multiple entities to the JSON file
     *
     * @param newEntities the list of entities to save
     * @return the complete list of entities after saving
     * @throws IOException if a file access error occurs
     */
    public List<T> saveAll(List<T> newEntities) throws IOException {
        List<T> entities = loadAll();
        entities.addAll(newEntities);
        JsonFileUtil.writeToJson(filePath, entities);
        return entities;
    }

    /**
     * Loads all entities from the JSON file
     *
     * @return a list of all entities
     * @throws IOException if a file access error occurs
     */
    public List<T> loadAll() throws IOException {
        return JsonFileUtil.readFromJson(filePath, typeReference);
    }

    /**
     * Finds a single entity matching the given predicate
     *
     * @param matcher the predicate to match entities against
     * @return an Optional containing the matched entity, or empty if none found
     * @throws IOException if a file access error occurs
     */
    public Optional<T> findOne(Predicate<T> matcher) throws IOException {
        return loadAll().stream().filter(matcher).findFirst();
    }

    /**
     * Finds all entities matching the given predicate
     *
     * @param matcher the predicate to match entities against
     * @return a list of all matching entities
     * @throws IOException if a file access error occurs
     */
    public List<T> findAll(Predicate<T> matcher) throws IOException {
        return loadAll().stream().filter(matcher).collect(Collectors.toList());
    }

    /**
     * Updates an entity matching the given predicate
     *
     * @param matcher the predicate to find the entity to update
     * @param updatedEntity the updated entity data
     * @return an Optional containing the updated entity, or empty if none found
     * @throws IOException if a file access error occurs
     */
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

    /**
     * Deletes entities matching the given predicate
     *
     * @param matcher the predicate to match entities to delete
     * @return true if any entities were deleted, false otherwise
     * @throws IOException if a file access error occurs
     */
    public boolean delete(Predicate<T> matcher) throws IOException {
        List<T> entities = loadAll();
        List<T> remaining = entities.stream().filter(matcher.negate()).collect(Collectors.toList());

        if (remaining.size() != entities.size()) {
            JsonFileUtil.writeToJson(filePath, remaining);
            return true;
        }

        return false;
    }

    /**
     * Checks if any entities match the given predicate
     *
     * @param matcher the predicate to match entities against
     * @return true if any matching entities exist, false otherwise
     * @throws IOException if a file access error occurs
     */
    public boolean exists(Predicate<T> matcher) throws IOException {
        return loadAll().stream().anyMatch(matcher);
    }

    /**
     * Counts the total number of entities
     *
     * @return the count of all entities
     * @throws IOException if a file access error occurs
     */
    public long count() throws IOException {
        return loadAll().size();
    }

    /**
     * Clears all entities from the JSON file
     *
     * @throws IOException if a file access error occurs
     */
    public void clear() throws IOException {
        JsonFileUtil.writeToJson(filePath, new ArrayList<T>());
    }

}
