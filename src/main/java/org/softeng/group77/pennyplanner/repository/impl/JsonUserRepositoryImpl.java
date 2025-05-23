package org.softeng.group77.pennyplanner.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.UserRepository;
import org.softeng.group77.pennyplanner.repository.base.JsonDataManager;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
/**
 * JSON-based implementation of the UserRepository.
 *
 * This repository handles user data persistence using a local JSON file.
 */
@Repository
public class JsonUserRepositoryImpl extends JsonDataManager<User> implements UserRepository {
    /**
     * Constructs the user repository with the path to the JSON data file.
     *
     * @param filePath the path to the user data JSON file,
     *                 configurable via application properties
     */
    public JsonUserRepositoryImpl(@Value("${app.data.path:data}/users.json") String filePath) {
        super(filePath, new TypeReference<>() {});
    }

    /**
     * Finds a user by email address (case-insensitive).
     *
     * @param email the email to search for
     * @return an Optional containing the user if found, or empty
     * @throws IOException if an error occurs while reading from the JSON file
     */
    @Override
    public Optional<User> findByEmail(String email) throws IOException {
        return findOne(user -> user.getEmail().equalsIgnoreCase(email));
    }
    /**
     * Finds a user by username (case-insensitive).
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, or empty
     * @throws IOException if an error occurs while reading from the JSON file
     */
    @Override
    public Optional<User> findByUsername(String username) throws IOException {
        return findOne(user -> user.getUsername().equalsIgnoreCase(username));
    }
    /**
     * Finds a user by their unique ID.
     *
     * @param id the ID of the user
     * @return an Optional containing the user if found, or empty
     * @throws IOException if an error occurs while reading from the JSON file
     */
    @Override
    public Optional<User> findById(String id) throws IOException {
        return findOne(user -> user.getId().equals(id));
    }
    /**
     * Loads all users from the JSON file.
     *
     * @return a list of all users
     * @throws IOException if an error occurs while reading from the JSON file
     */
    @Override
    public List<User> findAll() throws IOException {
        return loadAll();
    }
    /**
     * Saves a user by replacing any existing user with the same ID.
     *
     * @param newUser the user to save
     * @return the saved user
     * @throws IOException if an error occurs while writing to the JSON file
     */
    @Override
    public User save(User newUser) throws IOException {
        delete(existing -> existing.getId().equals(newUser.getId()));
        return super.save(newUser);
    }
    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     * @return true if a user was deleted, false otherwise
     * @throws IOException if an error occurs while modifying the JSON file
     */
    @Override
    public boolean deleteById(String id) throws IOException {
            return delete(user -> user.getId().equals(id));
    }

}
