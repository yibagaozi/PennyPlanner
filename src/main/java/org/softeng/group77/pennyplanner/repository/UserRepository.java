package org.softeng.group77.pennyplanner.repository;

import org.softeng.group77.pennyplanner.model.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username) throws IOException;

    Optional<User> findByEmail(String email) throws IOException;

    Optional<User> findById(String id) throws IOException;

    List<User> findAll() throws IOException;

    User save(User newUser) throws IOException;

    boolean deleteById(String id) throws IOException;

}
