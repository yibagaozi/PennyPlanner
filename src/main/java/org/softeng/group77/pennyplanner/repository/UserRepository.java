package org.softeng.group77.pennyplanner.repository;

import org.softeng.group77.pennyplanner.model.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides methods to create, retrieve, update and delete user accounts.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
public interface UserRepository {

    /**
     * Finds a user by their username
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     * @throws IOException if a data access error occurs
     */
    Optional<User> findByUsername(String username) throws IOException;

    /**
     * Finds a user by their email address
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     * @throws IOException if a data access error occurs
     */
    Optional<User> findByEmail(String email) throws IOException;

    /**
     * Finds a user by their ID
     *
     * @param id the user ID to search for
     * @return an Optional containing the user if found
     * @throws IOException if a data access error occurs
     */
    Optional<User> findById(String id) throws IOException;

    /**
     * Retrieves all users from the repository
     *
     * @return a list of all users
     * @throws IOException if a data access error occurs
     */
    List<User> findAll() throws IOException;

    /**
     * Saves a new user to the repository
     *
     * @param newUser the user to save
     * @return the saved user with any generated fields
     * @throws IOException if a data access error occurs
     */
    User save(User newUser) throws IOException;

    /**
     * Deletes a user by their ID
     *
     * @param id the ID of the user to delete
     * @return true if deletion was successful, false otherwise
     * @throws IOException if a data access error occurs
     */
    boolean deleteById(String id) throws IOException;

}
