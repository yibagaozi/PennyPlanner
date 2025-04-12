package org.softeng.group77.pennyplanner.util;

import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.model.User;

public class UserMapper {

    public static UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return new UserInfo(user);
    }

    public static User updateUserFromUserInfo(User user, UserInfo userInfo) {
        if (user == null || userInfo == null) return user;

        user.setUsername(userInfo.getUsername());
        user.setEmail(userInfo.getEmail());
        user.setPhone(userInfo.getPhone());

        return user;
    }
}
