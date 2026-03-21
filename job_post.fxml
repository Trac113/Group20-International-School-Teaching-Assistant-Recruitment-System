package com.qq.recruitment.controller;

import com.qq.recruitment.model.User;
import com.qq.recruitment.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = userService.login(username, password);

        if (user != null) {
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + user.getFullName() + "!");
            // TODO: Navigate to main dashboard based on role
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    @FXML
    public void handleRegister() {
         // simple register for demo
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if(username.isEmpty() || password.isEmpty()){
             showAlert(Alert.AlertType.ERROR, "Register Failed", "Username and password cannot be empty.");
             return;
        }

        boolean success = userService.register(username, password, "New User", "APPLICANT");
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User registered successfully. Please login.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username already exists.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
