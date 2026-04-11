package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.User;

import java.util.Optional;

public class UserService {
    private final JsonFileDAO userDAO;

    public UserService() {
        this.userDAO = new JsonFileDAO();
    }

    public boolean register(String username, String password, String fullName, String role) {
        if (userDAO.findUserByUsername(username).isPresent()) {
            return false; // User already exists
        }
        User newUser = new User(username, password, fullName, role);
        userDAO.addUser(newUser);
        return true;
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userDAO.findUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
