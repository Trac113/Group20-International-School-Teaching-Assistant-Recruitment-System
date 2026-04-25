package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.User;
import com.qq.recruitment.util.InputValidator;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final JsonFileDAO userDAO;

    public UserService() {
        this.userDAO = new JsonFileDAO();
    }

    public boolean register(String username, String password, String fullName, String role) {
        final String normalizedUsername;
        final String normalizedPassword;
        final String normalizedRole;
        final String normalizedFullName;
        try {
            normalizedUsername = InputValidator.normalizeUsername(username);
            normalizedPassword = InputValidator.normalizePassword(password);
            normalizedRole = InputValidator.normalizeRole(role);
            normalizedFullName = InputValidator.normalizeOptionalText(fullName, "Full name", 80, false);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        String finalFullName = normalizedFullName.isBlank() ? normalizedUsername : normalizedFullName;

        if (userDAO.findUserByUsername(normalizedUsername).isPresent()) {
            return false; // User already exists
        }
        User newUser = new User(normalizedUsername, normalizedPassword, finalFullName, normalizedRole);
        userDAO.addUser(newUser);
        return true;
    }

    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        final String normalizedUsername;
        final String normalizedPassword;
        try {
            normalizedUsername = InputValidator.normalizeUsername(username);
            normalizedPassword = InputValidator.normalizePassword(password);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        Optional<User> userOpt = userDAO.findUserByUsername(normalizedUsername);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(normalizedPassword)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public boolean deleteUser(String username) {
        Optional<User> existing = userDAO.findUserByUsername(username);
        if (existing.isEmpty()) {
            return false;
        }
        if ("ADMIN".equals(existing.get().getRole())) {
            return false;
        }
        return userDAO.deleteUser(username);
    }

    public boolean userExists(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return userDAO.findUserByUsername(username.trim()).isPresent();
    }

    public boolean updateUserRole(String username, String role) {
        Optional<User> userOpt = userDAO.findUserByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        user.setRole(role);
        return userDAO.updateUser(user);
    }

    public boolean updateUserPassword(String username, String newPassword) {
        final String normalizedUsername;
        final String normalizedPassword;
        try {
            normalizedUsername = InputValidator.normalizeUsername(username);
            normalizedPassword = InputValidator.normalizePassword(newPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        Optional<User> userOpt = userDAO.findUserByUsername(normalizedUsername);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        user.setPassword(normalizedPassword);
        return userDAO.updateUser(user);
    }

    public boolean updateFullName(String username, String fullName) {
        Optional<User> userOpt = userDAO.findUserByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        user.setFullName(fullName);
        return userDAO.updateUser(user);
    }
}
