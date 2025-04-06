package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.User;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public interface AuthService {

    User login(String username, String password) throws AuthenticationException;

    User register(String username, String password, String email);
}
