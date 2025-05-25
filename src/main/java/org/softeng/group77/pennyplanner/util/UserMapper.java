package org.softeng.group77.pennyplanner.util;

import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.model.User;

/**
 * Utility class for mapping between User model objects and UserInfo DTOs.
 * Handles conversion between internal data models and data transfer objects.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserMapper {

    /**
     * Converts a User model to a UserInfo DTO
     *
     * @param user the user model to convert
     * @return the corresponding UserInfo DTO or null if input is null
     */
    public static UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return new UserInfo(user);
    }

    /**
     * Updates a User model with data from a UserInfo DTO
     *
     * @param user the user model to update
     * @param userInfo the DTO containing updated information
     * @return the updated User model or original user if inputs are null
     */
    public static User updateUserFromUserInfo(User user, UserInfo userInfo) {
        if (user == null || userInfo == null) return user;

        user.setUsername(userInfo.getUsername());
        user.setEmail(userInfo.getEmail());
        user.setPhone(userInfo.getPhone());

        return user;
    }
}
