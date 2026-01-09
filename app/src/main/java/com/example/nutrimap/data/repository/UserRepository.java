package com.example.nutrimap.data.repository;

import com.example.nutrimap.domain.model.User;
import com.example.nutrimap.util.StaticDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for user data access.
 */
public class UserRepository {

    private static UserRepository instance;

    private UserRepository() {}

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public List<User> getAllUsers() {
        return StaticDataProvider.getInstance().getUsers();
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.isEmpty()) {
            return getAllUsers();
        }

        String lowerQuery = query.toLowerCase();
        List<User> result = new ArrayList<>();
        for (User u : getAllUsers()) {
            if (u.getName().toLowerCase().contains(lowerQuery) ||
                    u.getEmail().toLowerCase().contains(lowerQuery)) {
                result.add(u);
            }
        }
        return result;
    }

    public User getUserByEmail(String email) {
        return StaticDataProvider.getInstance().getUserByEmail(email);
    }

    public User addUser(User user) {
        return StaticDataProvider.getInstance().addUser(user);
    }

    public boolean validateLogin(String email, String password) {
        User user = getUserByEmail(email);
        return user != null && password.equals(user.getPassword());
    }
}
