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

@Slf4j
public class JsonFileUtil {

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static final ConcurrentHashMap<String, ReadWriteLock> fileLocks = new ConcurrentHashMap<>();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private static ReadWriteLock getFileLock(String filePath) {
        return fileLocks.computeIfAbsent(filePath, k -> new ReentrantReadWriteLock());
    }

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
    
    private static void ensureDirectoryExists(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

}
