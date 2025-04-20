package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.model.User;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.io.IOException;

@Component
public interface AuthService {

    UserInfo login(String username, String password) throws AuthenticationException;

    UserInfo register(String username, String password, String email, String phone) throws IOException;

    UserInfo getCurrentUser();

    UserInfo updateUserInfo(String userId, UserInfo updatedInfo);

    UserInfo changePassword(String userId, String oldPassword, String newPassword) throws AuthenticationException;

    void logout();

    boolean isLoggedIn();

}
