package org.softeng.group77.pennyplanner.util;

import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.io.IOException;

@Component
public class TestDataGenerateUtil {

    @Autowired
    private AuthService authService;

    public void generateTestUserData() throws IOException, AuthenticationException {
        authService.register("testuser","password","email@email.com","13333333333");
        authService.login("testuser","password");
        UserInfo user = authService.getCurrentUser();
        if (user == null) {
            System.out.println("Failed to load user data.");
        } else {
            System.out.println("Loaded user: " + user.getUsername());
        }
        System.out.println("Test user data generated: " + user.getUsername() + ", " + user.getEmail() + ", " + user.getPhone());
    }

}
