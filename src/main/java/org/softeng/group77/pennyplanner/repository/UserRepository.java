package org.softeng.group77.pennyplanner.repository;

import org.softeng.group77.pennyplanner.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    void save(User newUser);
}
