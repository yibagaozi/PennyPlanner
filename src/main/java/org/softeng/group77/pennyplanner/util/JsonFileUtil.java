package org.softeng.group77.pennyplanner.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Utility class for reading and writing JSON files with thread safety.
 * Handles JSON serialization and deserialization with proper file locking.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class JsonFileUtil {

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static final ConcurrentHashMap<String, ReadWriteLock> fileLocks = new ConcurrentHashMap<>();

    /**
     * Creates and configures an ObjectMapper with proper settings
     *
     * @return configured ObjectMapper instance
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Gets a lock for a specific file path
     *
     * @param filePath path to the file
     * @return a lock for the file
     */
    private static ReadWriteLock getFileLock(String filePath) {
        return fileLocks.computeIfAbsent(filePath, k -> new ReentrantReadWriteLock());
    }

    /**
     * Reads JSON data from a file and converts it to an object
     *
     * @param filePath path to the JSON file
     * @param clazz class to convert the JSON data to
     * @return object of type T or null if file doesn't exist
     * @throws IOException if reading or parsing fails
     */
    public static <T> T readFromJson(String filePath, Class<T> clazz) throws IOException {
        ReadWriteLock lock = getFileLock(filePath);
        lock.readLock().lock();
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                if (List.class.isAssignableFrom(clazz)) {
                    return (T) new ArrayList<>();
                }
                return null;
            }
            return objectMapper.readValue(path.toFile(), clazz);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Reads JSON data from a file using a TypeReference
     *
     * @param filePath path to the JSON file
     * @param typeReference type reference for complex types
     * @return object of type T or null if file doesn't exist
     * @throws IOException if reading or parsing fails
     */
    public static <T> T readFromJson(String filePath, TypeReference<T> typeReference) throws IOException {
        ReadWriteLock lock = getFileLock(filePath);
        lock.readLock().lock();
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                if (typeReference.getType().getTypeName().startsWith("java.util.List")) {
                    return (T) new ArrayList<>();
                }
                return null;
            }
            return objectMapper.readValue(path.toFile(), typeReference);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Writes data to a JSON file safely using a temporary file
     *
     * @param filePath path where to save the JSON file
     * @param data object to convert to JSON and save
     * @throws IOException if writing fails
     */
    public static void writeToJson(String filePath, Object data) throws IOException {
        ReadWriteLock lock = getFileLock(filePath);
        lock.writeLock().lock();
        try {
            Path path = Paths.get(filePath);
            ensureDirectoryExists(path);

            Path tempFile = Files.createTempFile(path.getParent(), "temp", ".json");

            objectMapper.writeValue(tempFile.toFile(), data);

            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Updates JSON data by reading, modifying, and writing it back
     *
     * @param filePath path to the JSON file
     * @param clazz class of the data
     * @param updater function to update the data
     * @throws IOException if reading or writing fails
     */
    public static <T> void updateJson(String filePath, Class<T> clazz, JsonUpdater<T> updater) throws IOException {
        ReadWriteLock lock = getFileLock(filePath);
        lock.writeLock().lock();
        try {
            T data = readFromJson(filePath, clazz);
            if (data == null) {
                data = clazz.getDeclaredConstructor().newInstance();
            }
            T updatedData = updater.update(data);
            writeToJson(filePath, updatedData);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Failed to create instance of " + clazz.getName(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Deletes a JSON file if it exists
     *
     * @param filePath path to the JSON file
     * @return true if file was deleted, false otherwise
     */
    public static boolean deleteJson(String filePath) {
        ReadWriteLock lock = getFileLock(filePath);
        lock.writeLock().lock();
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Makes sure the directory exists before writing to a file
     *
     * @param path path to the file
     * @throws IOException if directory creation fails
     */
    private static void ensureDirectoryExists(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Checks if a file exists
     *
     * @param filePath path to the file
     * @return true if file exists, false otherwise
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

}
