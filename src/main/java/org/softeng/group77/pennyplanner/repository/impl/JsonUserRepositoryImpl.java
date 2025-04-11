package org.softeng.group77.pennyplanner.repository.impl;

import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JsonUserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(String id) { return Optional.empty(); }

    @Override
    public List<User> findAll() { return new ArrayList<>(); }

    @Override
    public User save(User newUser) { return newUser; }

    @Override
    public boolean deleteById(String id) { return false; }

}
