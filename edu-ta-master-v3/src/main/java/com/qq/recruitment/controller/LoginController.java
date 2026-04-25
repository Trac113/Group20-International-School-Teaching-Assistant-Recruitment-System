package com.qq.recruitment.controller;

import com.qq.recruitment.App;
import com.qq.recruitment.model.User;
import com.qq.recruitment.service.UserService;
import com.qq.recruitment.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("APPLICANT", "TEACHER", "ADMIN"));
        roleComboBox.setValue("APPLICANT");
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isBlank() || password.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Username and password cannot be blank.");
            return;
        }

        User user = userService.login(username, password);

        if (user != null) {
            if (role != null && !role.equals(user.getRole())) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Role mismatch for this account.");
                return;
            }
            SessionManager.getInstance().login(user);
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + user.getUsername() + "!");
            try {
                // Navigate to main layout after successful login
                App.setRoot("main");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to main dashboard.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    @FXML
    public void handleRegister() {
        String rawUsername = usernameField.getText() == null ? "" : usernameField.getText();
        String rawPassword = passwordField.getText() == null ? "" : passwordField.getText();
        String username = rawUsername.trim();
        String password = rawPassword.trim();
        String fullName = username;
        String role = roleComboBox.getValue();

        if (username.isBlank() || password.isBlank() || role == null || role.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Register Failed", "Username, password and role are required.");
            return;
        }
        if (containsWhitespace(rawUsername) || containsWhitespace(rawPassword)) {
            showAlert(Alert.AlertType.ERROR, "Register Failed", "Username and password cannot contain spaces.");
            return;
        }
        if (!isAsciiOnly(rawUsername) || !isAsciiOnly(rawPassword)) {
            showAlert(Alert.AlertType.ERROR, "Register Failed", "Username and password must be ASCII characters only.");
            return;
        }

        boolean success = userService.register(username, password, fullName, role);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User registered successfully as " + role + ". Please login.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username already exists or input format is invalid.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean containsWhitespace(String value) {
        return value != null && value.chars().anyMatch(Character::isWhitespace);
    }

    private boolean isAsciiOnly(String value) {
        return value != null && value.chars().allMatch(ch -> ch <= 127);
    }
}
