package org.softeng.group77.pennyplanner.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.UserRepository;
import org.softeng.group77.pennyplanner.repository.base.JsonDataManager;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Repository
public class JsonUserRepositoryImpl extends JsonDataManager<User> implements UserRepository {

    public JsonUserRepositoryImpl(@Value("${app.data.path:data}/users.json") String filePath) {
        super(filePath, new TypeReference<>() {});
    }

    @Override
    public Optional<User> findByEmail(String email) throws IOException {
        return findOne(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public Optional<User> findByUsername(String username) throws IOException {
        return findOne(user -> user.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public Optional<User> findById(String id) throws IOException {
        return findOne(user -> user.getId().equals(id));
    }

    @Override
    public List<User> findAll() throws IOException {
        return loadAll();
    }

    @Override
    public User save(User newUser) throws IOException {
        delete(existing -> existing.getId().equals(newUser.getId()));
        return super.save(newUser);
    }

    @Override
    public boolean deleteById(String id) throws IOException {
            return delete(user -> user.getId().equals(id));
    }

}
