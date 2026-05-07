package com.qq.recruitment.controller;

import com.qq.recruitment.model.User;
import com.qq.recruitment.service.UserService;
import com.qq.recruitment.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

/**
 * Teacher account screen for editing the teacher's display name.
 */
public class TeacherAccountController {
    @FXML
    private TextField fullNameField;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        }
    }

    @FXML
    private void handleSave() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Not logged in.");
            return;
        }

        String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        if (fullName.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Full name cannot be blank.");
            return;
        }

        if (userService.updateFullName(currentUser.getUsername(), fullName)) {
            currentUser.setFullName(fullName);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Name updated successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update name.");
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
