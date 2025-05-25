package org.softeng.group77.pennyplanner.util;

/**
 * Functional interface for updating JSON data.
 * Used with JsonFileUtil to modify data during read-update-write operations.
 *
 * @param <T> the type of data to update
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface JsonUpdater<T> {

    /**
     * Updates the provided data object
     *
     * @param data the data to update
     * @return the updated data
     */
    T update (T data);

}
