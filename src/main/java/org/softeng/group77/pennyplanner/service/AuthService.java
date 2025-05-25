package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.exception.RegistrationException;
import org.softeng.group77.pennyplanner.model.User;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.io.IOException;

/**
 * Provides authentication and user management operations.
 * Handles user login, registration, and profile management.
 *
 * @author MA Ruize
 * @author CHAI Yihang
 * @version 2.0.0
 * @since 1.0.0
 */
@Component
public interface AuthService {

    /**
     * Authenticates a user with username and password
     *
     * @param username the user's username
     * @param password the user's password
     * @return user information if authentication successful
     * @throws AuthenticationException if credentials are invalid
     */
    UserInfo login(String username, String password) throws AuthenticationException;

    /**
     * Registers a new user account
     *
     * @param username the desired username
     * @param password the account password
     * @param email the user's email address
     * @param phone the user's phone number
     * @return the created user information
     * @throws IOException if storage operation fails
     */
    UserInfo register(String username, String password, String email, String phone) throws IOException;

    /**
     * Gets the currently authenticated user
     *
     * @return user information or null if no user is logged in
     */
    UserInfo getCurrentUser();

    /**
     * Updates a user's profile information
     *
     * @param userId the ID of the user to update
     * @param updatedInfo the new user information
     * @return the updated user information
     * @throws RegistrationException if updated information is invalid
     * @throws IOException if storage operation fails
     */
    UserInfo updateUserInfo(String userId, UserInfo updatedInfo) throws RegistrationException, IOException;

    /**
     * Changes a user's password
     *
     * @param userId the ID of the user
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @return the updated user information
     * @throws AuthenticationException if old password is incorrect
     */
    UserInfo changePassword(String userId, String oldPassword, String newPassword) throws AuthenticationException;

    /**
     * Logs out the current user
     */
    void logout();

    /**
     * Checks if a user is currently logged in
     *
     * @return true if a user is logged in, false otherwise
     */
    boolean isLoggedIn();

}
